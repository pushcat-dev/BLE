package com.example.blecompose.feature.bluetooth.domain.entity

data class DomainTapScanResult(
    val name: String?,
    val address: String,
    val rssi: Int,
    val isConnectable: Boolean
)