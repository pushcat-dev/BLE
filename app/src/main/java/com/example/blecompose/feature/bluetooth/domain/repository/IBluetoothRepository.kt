package com.example.blecompose.feature.bluetooth.domain.repository

import android.bluetooth.BluetoothDevice
import com.example.blecompose.core.bluetooth.service.TapBleService
import com.example.blecompose.feature.bluetooth.domain.entity.DomainTapScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothRepository {

    // Scan
    fun startScan(): Flow<List<DomainTapScanResult>>
    fun stopScan()

    // Connection
    fun connect(device: BluetoothDevice)
    fun disconnect(device: BluetoothDevice)
    fun disconnectAll()
    fun connectedDevices(): StateFlow<List<BluetoothDevice>>

    // Notify merged (address + bytes)
    fun mergedNotify(): Flow<Pair<String, ByteArray>>

    // Write
    fun send(address: String, bytes: ByteArray)
    fun sendAll(bytes: ByteArray)

    // Service Control
    fun startBleService()

    fun bindService(service: TapBleService)
    fun unbindService()
}