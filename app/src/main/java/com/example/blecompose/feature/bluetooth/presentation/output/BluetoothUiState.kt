package com.example.blecompose.feature.bluetooth.presentation.output

import android.bluetooth.BluetoothDevice
import com.example.blecompose.feature.bluetooth.domain.entity.DomainTapScanResult


data class BluetoothUiState(
    val scanning: Boolean = false,
    val scanResults: List<DomainTapScanResult> = emptyList(),
    val connectedDevices: List<BluetoothDevice> = emptyList(),
    val lightStates: Map<String, Boolean> = emptyMap(),
    val notifyLogs: List<Pair<String, ByteArray>> = emptyList()
)