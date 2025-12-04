package com.example.blecompose.feature.bluetooth.data.datasource

import android.bluetooth.BluetoothDevice
import com.example.blecompose.core.bluetooth.repository.MultiTapRepository
import com.example.blecompose.core.bluetooth.service.TapBleService
import com.example.blecompose.feature.bluetooth.data.mapper.toDomain
import com.example.blecompose.feature.bluetooth.domain.entity.BluetoothDeviceEntity
import com.example.blecompose.feature.bluetooth.domain.entity.DomainTapScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BluetoothDataSourceImpl @Inject constructor(
    private val coreRepo: MultiTapRepository
) : BluetoothDataSource {

    override fun startScan(): Flow<List<DomainTapScanResult>> =
        coreRepo.startScan()
            .map { list -> list.map { it.toDomain() } }

    override fun stopScan() = coreRepo.stopScan()

    override fun connect(device: BluetoothDevice) =
        coreRepo.connect(device)

    override fun disconnect(device: BluetoothDevice) =
        coreRepo.disconnect(device)

    override fun disconnectAll() = coreRepo.disconnectAll()

    override fun observeConnectedDevices(): Flow<List<BluetoothDeviceEntity>> =
        coreRepo.connectedDevices()
            .map { list -> list.map { it.toDomain() } }

    override fun observeNotify(): Flow<Pair<String, ByteArray>> =
        coreRepo.mergedNotify()

    override fun send(address: String, bytes: ByteArray) =
        coreRepo.send(address, bytes)

    override fun sendAll(bytes: ByteArray) =
        coreRepo.sendAll(bytes)

    override fun startBleService() =
        coreRepo.startBleService()

    override fun bindService(service: TapBleService) =
        coreRepo.bindService(service)

    override fun unbindService() =
        coreRepo.unbindService()
}