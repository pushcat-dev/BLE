package com.example.blecompose.core.bluetooth.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.blecompose.R
import com.example.blecompose.core.bluetooth.connection.TapBleConnection
import com.example.blecompose.core.bluetooth.model.TapScanResult
import com.example.blecompose.core.bluetooth.scanner.TapScanner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@AndroidEntryPoint
class TapBleService : Service() {

    @Inject lateinit var scanner: TapScanner
    @Inject lateinit var connection: TapBleConnection

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    inner class LocalBinder : Binder() {
        fun getService(): TapBleService = this@TapBleService
    }
    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        Log.d("simsim", "🧩 Service onBind 호출됨")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("simsim", "🚀 TapBleService 생성됨")
        startForegroundNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("simsim", "💥 TapBleService 종료됨 → disconnectAll + scope cancel")
        serviceScope.cancel()
        connection.disconnectAll()
    }

    private fun startForegroundNotification() {
        Log.d("simsim", "🔔 Foreground Notification 시작")

        val channelId = "tap_ble_service"
        val channelName = "Tap BLE Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("simsim", "🔔 NotificationChannel 생성")
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("BLE Running")
                .setContentText("MotionTap BLE 연결 유지 중")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()

        startForeground(9999, notification)
    }

    //──────────── Scan ──────────────────────────────
    fun startScan(): Flow<List<TapScanResult>> {
        Log.d("simsim", "🔍 startScan() 호출됨 (Service)")
        return scanner.startScan()
    }

    fun stopScan() {
        Log.d("simsim", "🛑 stopScan() 호출됨 (Service)")
        scanner.stopScan()
    }

    //──────────── Connection ──────────────────────────
    fun connect(device: BluetoothDevice) {
        Log.d("simsim", "🔗 connect(${device.address}) 요청됨")
        connection.connect(device)
    }

    fun disconnect(device: BluetoothDevice) {
        Log.d("simsim", "🔌 disconnect(${device.address}) 요청됨")
        connection.disconnect(device)
    }

    fun disconnectAll() {
        Log.d("simsim", "🔌 disconnectAll() 호출됨")
        connection.disconnectAll()
    }

    fun connectedDevices(): StateFlow<List<BluetoothDevice>> =
        connection.connectedDevices

    //──────────── Notify Merge ────────────────────────
    fun mergedNotifyFlow(): Flow<Pair<String, ByteArray>> =
        connection.connectedDevices
            .onEach { list ->
                Log.d("simsim", "📨 mergedNotifyFlow → 연결된 기기수 = ${list.size}")
            }
            .flatMapLatest { devices ->
                if (devices.isEmpty()) {
                    Log.d("simsim", "📨 연결된 기기 없음 → emptyFlow")
                    emptyFlow()
                } else {
                    val flows = devices.map { device ->
                        Log.d("simsim", "📨 notifyFlow 등록 → ${device.address}")
                        connection.notifyFlow(device.address) ?: emptyFlow()
                    }
                    merge(*flows.toTypedArray())
                }
            }
            .flowOn(Dispatchers.IO)

    //──────────── Send ────────────────────────────────
    fun send(address: String, data: ByteArray) {
        Log.d("simsim", "✉️ send → $address, size=${data.size}")
        connection.sendTo(address, data)
    }

    fun sendAll(data: ByteArray) {
        Log.d("simsim", "✉️ sendAll → 모든 기기 ${data.size} bytes")
        connection.sendToAll(data)
    }

    //──────────── Location Check ───────────────────────
    fun startScanWithCheck(): Flow<List<TapScanResult>> = callbackFlow {
        if (!TapScanner.checkLocationEnabled(this@TapBleService)) {
            Log.e("simsim", "⚠ 위치 꺼져있음 → 스캔 불가")
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        Log.d("simsim", "🔍 startScanWithCheck → 위치 OK → 스캔 시작")

        scanner.startScan().collect { trySend(it) }
    }
}