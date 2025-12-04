package com.example.blecompose.feature.bluetooth.domain.useCase

import com.example.blecompose.feature.bluetooth.domain.repository.BluetoothRepository
import javax.inject.Inject

class SendAllUseCase @Inject constructor(
    private val repo: BluetoothRepository
) {
    operator fun invoke(data: ByteArray) = repo.sendAll(data)
}