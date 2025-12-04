package com.example.blecompose.core.bluetooth.model


data class TapScanResult(
    val name: String,
    val address: String,
    val rssi: Int,
    val isConnectable: Boolean,
    val manufacturerData: ByteArray?
)