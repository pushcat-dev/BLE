package com.example.blecompose.core.bluetooth.connection

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.example.blecompose.core.bluetooth.constant.GameCommand
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TapBleConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val managers = mutableMapOf<String, TapBleManager>()

    private val _connectedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val connectedDevices: StateFlow<List<BluetoothDevice>> = _connectedDevices

    private val _deviceReady = MutableSharedFlow<BluetoothDevice>()
    val deviceReady = _deviceReady.asSharedFlow()

    private fun newManager(): TapBleManager = TapBleManager(context)

    //──────────────────────────────────────────────────────
    fun connect(device: BluetoothDevice) {
        val addr = device.address

        if (managers.containsKey(addr)) return

        val manager = newManager()
        manager.autoReconnectEnabled = false // 🔥 기본은 재연결 OFF
        managers[addr] = manager

        observeReady(manager)

        manager.connect(device)
            .useAutoConnect(false)
            .done {
                updateList()
            }
            .fail { _, _ ->
                managers.remove(addr)
                updateList()
            }
            .enqueue()
    }

    //──────────────────────────────────────────────────────
    fun disconnect(device: BluetoothDevice) {
        val addr = device.address
        val mgr = managers[addr] ?: return

        mgr.autoReconnectEnabled = false // 🔥 반드시 OFF

        mgr.disconnect()
            .done {
                Log.d("simsim", "🛑 Disconnected → $addr")
                mgr.close()
                managers.remove(addr)
                updateList()
            }
            .enqueue()
    }

    fun disconnectAll() {
        managers.values.forEach { mgr ->
            mgr.autoReconnectEnabled = false
            mgr.disconnect().enqueue()
            mgr.close()
        }
        managers.clear()
        updateList()
    }

    //──────────────────────────────────────────────────────
    private fun updateList() {
        _connectedDevices.value = managers.values.mapNotNull { it.device }
    }

    //──────────────────────────────────────────────────────
    fun notifyFlow(address: String): Flow<Pair<String, ByteArray>>? {
        val mgr = managers[address] ?: return null

        return merge(mgr.uartFlow, mgr.sensorFlow)
            .map { bytes -> address to bytes }
    }

    //──────────────────────────────────────────────────────
    private fun observeReady(manager: TapBleManager) {
        manager.readyFlow
            .onEach { device ->
                val addr = device.address

                // 1) 배터리 요청
                sendHexTo(addr, GameCommand.TAP_BATTERY_CODE)

                // 2) 연결 LED 점등
                val connectLedHex =
                    GameCommand.LED_CODE + "00" +
                            GameCommand.GAME_TAP_LED_CONNECT_CODE + "0000"

                Log.d("simsim", "💡 Auto Connect LED → $addr")
                sendHexTo(addr, connectLedHex)

                updateList()
                _deviceReady.emit(device)
            }
            .launchIn(GlobalScope)
    }

    //──────────────────────────────────────────────────────
    fun sendTo(address: String, bytes: ByteArray) {
        managers[address]?.send(bytes)
    }

    fun sendHexTo(address: String, hex: String) {
        managers[address]?.sendHex(hex)
    }

    fun sendToAll(bytes: ByteArray) {
        managers.values.forEach { it.send(bytes) }
    }
}