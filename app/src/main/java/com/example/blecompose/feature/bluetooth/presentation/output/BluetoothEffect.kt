package com.example.blecompose.feature.bluetooth.presentation.output

sealed interface BluetoothEffect {
    data class ShowToast(val message: String): BluetoothEffect
}