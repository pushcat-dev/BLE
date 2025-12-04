package com.example.blecompose.core.bluetooth.repository

import android.bluetooth.BluetoothDevice
import com.example.blecompose.core.bluetooth.model.TapScanResult
import com.example.blecompose.core.bluetooth.service.TapBleService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * BLE 기능을 추상화한 Repository 인터페이스
 * Feature 레이어는 이 인터페이스에만 의존한다.
 */
interface MultiTapRepository {

    // Scan
    fun startScan(): Flow<List<TapScanResult>>
    fun stopScan()

    // Connection
    fun connect(device: BluetoothDevice)
    fun disconnect(device: BluetoothDevice)
    fun disconnectAll()
    fun connectedDevices(): StateFlow<List<BluetoothDevice>>

    // Notify
    fun mergedNotify(): Flow<Pair<String, ByteArray>>

    // Write
    fun send(address: String, bytes: ByteArray)
    fun sendAll(bytes: ByteArray)

    // Service
    fun startBleService()
    fun bindService(service: TapBleService)
    fun unbindService()
}