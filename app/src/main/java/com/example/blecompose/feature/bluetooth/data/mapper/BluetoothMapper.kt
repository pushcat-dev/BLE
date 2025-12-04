package com.example.blecompose.feature.bluetooth.data.mapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import com.example.blecompose.core.bluetooth.model.TapScanResult
import com.example.blecompose.feature.bluetooth.domain.entity.BluetoothDeviceEntity
import com.example.blecompose.feature.bluetooth.domain.entity.DomainTapScanResult

fun TapScanResult.toDomain(): DomainTapScanResult =
    DomainTapScanResult(
        name = name,
        address = address,
        rssi = rssi,
        isConnectable = isConnectable
    )

@SuppressLint("MissingPermission")
fun BluetoothDevice.toDomain(): BluetoothDeviceEntity {
    val safeName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        try { name } catch (e: SecurityException) { null }
    } else name

    return BluetoothDeviceEntity(
        name = safeName ?: "Unknown",
        address = address
    )
}