package com.example.blecompose.core.bluetooth.connection

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ConnectionPriorityRequest
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.util.UUID
import javax.inject.Inject

class TapBleManager @Inject constructor(
    @ApplicationContext context: Context
) : BleManager(context), ConnectionObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var uartWrite: BluetoothGattCharacteristic? = null
    private var uartNotify: BluetoothGattCharacteristic? = null

    private val _uartFlow = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val uartFlow = _uartFlow.asSharedFlow()

    private val _batteryFlow = MutableSharedFlow<Int>(extraBufferCapacity = 16)
    val batteryFlow = _batteryFlow.asSharedFlow()

    private val _sensorFlow = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val sensorFlow = _sensorFlow.asSharedFlow()

    private val _readyFlow = MutableSharedFlow<BluetoothDevice>(extraBufferCapacity = 1)
    val readyFlow = _readyFlow.asSharedFlow()

    var autoReconnectEnabled = false // 🔥 외부에서 제어

    private var lastDevice: BluetoothDevice? = null

    init {
        setConnectionObserver(this)
    }

    override fun getGattCallback(): BleManagerGattCallback = GattCallback()

    //──────────────────────────────────────────────────────
    private inner class GattCallback : BleManagerGattCallback() {

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val addr = gatt.device.address
            Log.d("TapBle", "[$addr] 🔍 Service discovery")

            val service = gatt.getService(UART_SERVICE_UUID)

            uartWrite = service?.getCharacteristic(UART_WRITE_UUID)
            uartNotify = service?.getCharacteristic(UART_NOTIFY_UUID)

            val ok = uartWrite != null && uartNotify != null
            Log.d("TapBle", "[$addr] Service OK? → $ok")

            return ok
        }

        override fun initialize() {
            val addr = lastDevice?.address
            Log.d("TapBle", "[$addr] 🚀 initialize()")

            setNotificationCallback(uartNotify).with(::onUartNotify)

            enableNotifications(uartNotify)
                .done { Log.d("TapBle", "[$addr] NOTIFY OK") }
                .enqueue()

            requestMtu(244)
                .done { Log.d("TapBle", "[$addr] MTU=244 OK") }
                .fail { _, _ -> Log.e("TapBle", "[$addr] MTU FAIL") }
                .enqueue()

            scope.launch {
                delay(150)
                requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH)
                    .done { Log.d("TapBle", "[$addr] HIGH PRIORITY OK") }
                    .enqueue()
            }
        }

        override fun onServicesInvalidated() {
            Log.w("TapBle", "⚠ Services invalidated")
            uartWrite = null
            uartNotify = null
        }
    }

    //──────────────────────────────────────────────────────
    private fun onUartNotify(device: BluetoothDevice, data: Data) {
        val bytes = data.value ?: return
        scope.launch { _uartFlow.emit(bytes) }

        if (bytes.size >= 3 && bytes[0] == 0x1B.toByte() && bytes[1] == 0x41.toByte()) {
            val batt = bytes[2].toInt() and 0xFF
            scope.launch { _batteryFlow.emit(batt) }
        }

        if (bytes.size >= 2 && bytes[0] == 0x1B.toByte() && bytes[1] == 0x54.toByte()) {
            scope.launch { _sensorFlow.emit(bytes) }
        }
    }

    //──────────────────────────────────────────────────────
    override fun onDeviceConnected(device: BluetoothDevice) {
        lastDevice = device
        Log.d("TapBle", "Connected → ${device.address}")
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        lastDevice = device
        Log.d("TapBle", "READY → ${device.address}")
        scope.launch { _readyFlow.emit(device) }
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Log.e("TapBle", "❌ FailedToConnect(${device.address}) reason=$reason")

        if (autoReconnectEnabled) {
            attemptReconnect()
        } else {
            Log.d("TapBle", "❌ AutoReconnect disabled → skip")
        }
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        Log.w("TapBle", "⚠ Disconnected(${device.address})")

        if (autoReconnectEnabled) {
            attemptReconnect()
        } else {
            Log.d("TapBle", "AutoReconnect disabled → no retry")
        }
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.d("TapBle", "⏳ CONNECTING → ${device.address}")
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        Log.d("TapBle", "⏳ DISCONNECTING → ${device.address}")
    }

    private fun attemptReconnect() {
        val d = lastDevice ?: return
        connect(d)
            .useAutoConnect(false)
            .retry(3, 500)
            .timeout(15000)
            .enqueue()
    }

    fun send(bytes: ByteArray) {
        uartWrite?.let {
            writeCharacteristic(it, bytes, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
                .enqueue()
        }
    }

    fun sendHex(hex: String) =
        send(hexStringToByteArray(hex))

    val device: BluetoothDevice?
        get() = lastDevice

    companion object {
        val UART_SERVICE_UUID = uuid("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        val UART_WRITE_UUID = uuid("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        val UART_NOTIFY_UUID = uuid("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")

        fun uuid(str: String) = UUID.fromString(str)

        fun hexStringToByteArray(s: String): ByteArray {
            val data = ByteArray(s.length / 2)
            for (i in s.indices step 2) {
                data[i / 2] =
                    ((Character.digit(s[i], 16) shl 4) +
                            Character.digit(s[i + 1], 16)).toByte()
            }
            return data
        }
    }
}