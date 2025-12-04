package com.example.blecompose.feature.bluetooth.domain.useCase

import com.example.blecompose.core.bluetooth.constant.GameCommand
import com.example.blecompose.feature.bluetooth.domain.repository.BluetoothRepository
import javax.inject.Inject

class LightEachUseCase @Inject constructor(
    private val repo: BluetoothRepository
) {

    operator fun invoke(address: String, isOn: Boolean) {

        val hex = if (isOn) {
            // ON
            GameCommand.LED_CODE +
                    GameCommand.DEFAULT_VALUE +
                    "030000"
        } else {
            // OFF
            GameCommand.LED_CODE +
                    GameCommand.DEFAULT_VALUE +
                    "000000"
        }

        repo.send(address, hexToBytes(hex))
    }

    private fun hexToBytes(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}