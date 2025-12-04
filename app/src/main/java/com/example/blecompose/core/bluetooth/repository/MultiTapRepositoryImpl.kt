package com.example.blecompose.core.bluetooth.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.blecompose.core.bluetooth.model.TapScanResult
import com.example.blecompose.core.bluetooth.service.TapBleService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MultiTapRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MultiTapRepository {

    /** 핵심: Service Flow */
    private val _serviceFlow = MutableStateFlow<TapBleService?>(null)
    private val serviceFlow = _serviceFlow.asStateFlow()

    /** Service 바인딩 */
    override fun bindService(service: TapBleService) {
        _serviceFlow.value = service
    }

    override fun unbindService() {
        _serviceFlow.value = null
    }

    // -------------------------------------------------------
    // Scan
    // -------------------------------------------------------
    override fun startScan(): Flow<List<TapScanResult>> =
        serviceFlow.filterNotNull()
            .flatMapLatest { it.startScan() }

    override fun stopScan() {
        _serviceFlow.value?.stopScan()
    }

    // -------------------------------------------------------
    // Connection
    // -------------------------------------------------------
    override fun connect(device: BluetoothDevice) {
        _serviceFlow.value?.connect(device)
    }

    override fun disconnect(device: BluetoothDevice) {
        _serviceFlow.value?.disconnect(device)
    }

    override fun disconnectAll() {
        _serviceFlow.value?.disconnectAll()
    }

    override fun connectedDevices(): StateFlow<List<BluetoothDevice>> =
        serviceFlow.filterNotNull()
            .flatMapLatest { it.connectedDevices() }
            .stateIn(
                GlobalScope,
                SharingStarted.Eagerly,
                emptyList()
            )

    // -------------------------------------------------------
    // Notify (핵심)
    // -------------------------------------------------------
    override fun mergedNotify(): Flow<Pair<String, ByteArray>> =
        serviceFlow.filterNotNull()
            .flatMapLatest { service ->
                service.mergedNotifyFlow()
            }

    // -------------------------------------------------------
    // WRITE
    // -------------------------------------------------------
    override fun send(address: String, bytes: ByteArray) {
        _serviceFlow.value?.send(address, bytes)
    }

    override fun sendAll(bytes: ByteArray) {
        _serviceFlow.value?.sendAll(bytes)
    }

    // -------------------------------------------------------
    // Service Control
    // -------------------------------------------------------
    override fun startBleService() {
        val i = Intent(context, TapBleService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(i)
        else
            context.startService(i)
    }
}