package com.example.blecompose.feature.bluetooth.domain.useCase

import android.bluetooth.BluetoothAdapter
import com.example.blecompose.feature.bluetooth.domain.entity.DomainTapScanResult
import com.example.blecompose.feature.bluetooth.domain.repository.BluetoothRepository
import javax.inject.Inject

class AutoConnectUseCase @Inject constructor(
    private val repo: BluetoothRepository
) {
    operator fun invoke(devices: List<DomainTapScanResult>) {
        val adapter = BluetoothAdapter.getDefaultAdapter()

        devices.forEach { scan ->
            val device = adapter.getRemoteDevice(scan.address)
            repo.connect(device)
        }
    }
}