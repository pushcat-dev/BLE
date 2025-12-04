package com.example.blecompose.feature.bluetooth.domain.useCase

import android.bluetooth.BluetoothDevice
import com.example.blecompose.feature.bluetooth.domain.repository.BluetoothRepository
import javax.inject.Inject

class DisconnectUseCase @Inject constructor(
    private val repo: BluetoothRepository
) {
    operator fun invoke(device: BluetoothDevice) = repo.disconnect(device)
}