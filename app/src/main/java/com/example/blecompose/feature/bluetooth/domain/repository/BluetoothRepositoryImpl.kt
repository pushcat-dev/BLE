package com.example.blecompose.feature.bluetooth.domain.repository

import android.bluetooth.BluetoothDevice
import com.example.blecompose.core.bluetooth.repository.MultiTapRepository
import com.example.blecompose.core.bluetooth.service.TapBleService
import com.example.blecompose.feature.bluetooth.domain.entity.DomainTapScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothRepositoryImpl @Inject constructor(
    private val coreRepo: MultiTapRepository
) : BluetoothRepository {

    // -------------------------------------------------------
    // Scan
    // -------------------------------------------------------
    override fun startScan(): Flow<List<DomainTapScanResult>> {
        return coreRepo.startScan().map { list ->
            list.map {
                DomainTapScanResult(
                    name = it.name,
                    address = it.address,
                    rssi = it.rssi,
                    isConnectable = it.isConnectable
                )
            }
        }
    }

    override fun stopScan() = coreRepo.stopScan()

    // -------------------------------------------------------
    // Connections
    // -------------------------------------------------------
    override fun connect(device: BluetoothDevice) =
        coreRepo.connect(device)

    override fun disconnect(device: BluetoothDevice) =
        coreRepo.disconnect(device)

    override fun disconnectAll() =
        coreRepo.disconnectAll()

    override fun connectedDevices(): StateFlow<List<BluetoothDevice>> =
        coreRepo.connectedDevices()

    // -------------------------------------------------------
    // Notify
    // -------------------------------------------------------
    override fun mergedNotify(): Flow<Pair<String, ByteArray>> =
        coreRepo.mergedNotify()

    // -------------------------------------------------------
    // Write
    // -------------------------------------------------------
    override fun send(address: String, bytes: ByteArray) =
        coreRepo.send(address, bytes)

    override fun sendAll(bytes: ByteArray) =
        coreRepo.sendAll(bytes)

    // -------------------------------------------------------
    // Service Control
    // -------------------------------------------------------
    override fun startBleService() =
        coreRepo.startBleService()

    override fun bindService(service: TapBleService) =
        coreRepo.bindService(service)

    override fun unbindService() =
        coreRepo.unbindService()
}