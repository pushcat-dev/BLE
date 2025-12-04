package com.example.blecompose.feature.bluetooth.domain.useCase

import com.example.blecompose.core.bluetooth.service.TapBleService
import com.example.blecompose.core.bluetooth.repository.MultiTapRepositoryImpl
import com.example.blecompose.feature.bluetooth.domain.repository.BluetoothRepository
import javax.inject.Inject

class BindServiceUseCase @Inject constructor(
    private val repo: BluetoothRepository
) {
    operator fun invoke(service: TapBleService) =
        (repo as? MultiTapRepositoryImpl)?.bindService(service)
}