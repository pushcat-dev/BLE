package com.example.blecompose.feature.bluetooth.data.datasource

import android.bluetooth.BluetoothDevice
import com.example.blecompose.core.bluetooth.service.TapBleService
import com.example.blecompose.feature.bluetooth.domain.entity.BluetoothDeviceEntity
import com.example.blecompose.feature.bluetooth.domain.entity.DomainTapScanResult
import kotlinx.coroutines.flow.Flow

interface BluetoothDataSource {

    fun startScan(): Flow<List<DomainTapScanResult>>
    fun stopScan()

    fun connect(device: BluetoothDevice)
    fun disconnect(device: BluetoothDevice)
    fun disconnectAll()

    fun observeConnectedDevices(): Flow<List<BluetoothDeviceEntity>>
    fun observeNotify(): Flow<Pair<String, ByteArray>>

    fun send(address: String, bytes: ByteArray)
    fun sendAll(bytes: ByteArray)

    fun startBleService()
    fun bindService(service: TapBleService)
    fun unbindService()
}