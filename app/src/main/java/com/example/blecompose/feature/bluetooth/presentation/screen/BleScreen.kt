package com.example.blecompose.feature.bluetooth.presentation.screen

import com.example.blecompose.feature.bluetooth.presentation.input.BluetoothInput
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blecompose.feature.bluetooth.presentation.output.BluetoothUiState
import com.example.blecompose.feature.bluetooth.presentation.component.*

@Composable
fun BleScreen(
    uiState: BluetoothUiState,
    onInput: (BluetoothInput) -> Unit,
    modifier: Modifier = Modifier
) {
    @SuppressLint("MissingPermission")
    fun safeDeviceName(device: BluetoothDevice): String {
        return try {
            device.name ?: "Unknown"
        } catch (e: SecurityException) {
            "Unknown"
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ------------------------------------------------------
        // Header
        // ------------------------------------------------------
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("탭 연결", style = MaterialTheme.typography.titleLarge)

        }

        Spacer(Modifier.height(16.dp))

        // ------------------------------------------------------
        // Connected Tabs
        // ------------------------------------------------------
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("연결된 탭 (${uiState.connectedDevices.size})")

            Row {
                Button(onClick = { /* 등록 */ }) { Text("등록") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { /* 전체 점등 */ }) { Text("전체점등") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onInput(BluetoothInput.DisconnectAll) }) {
                    Text("연결해제")
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyRow {
            items(uiState.connectedDevices) { device ->

                val isOn = uiState.lightStates[device.address] ?: false

                ConnectedTabItem(
                    name = safeDeviceName(device),
                    isLightOn = isOn,
                    onLightOn = { onInput(BluetoothInput.LightOn(device.address)) },
                    onLightOff = { onInput(BluetoothInput.LightOff(device.address)) },
                    onDisconnect = { onInput(BluetoothInput.Disconnect(device)) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ------------------------------------------------------
        // Scannable Tabs
        // ------------------------------------------------------
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("연결 가능한 탭 (${uiState.scanResults.size})")

            Row {
                val scanningText = if (uiState.scanning) "검색중" else "검색"
                Button(
                    onClick = {
                        if (uiState.scanning) onInput(BluetoothInput.StopScan)
                        else onInput(BluetoothInput.StartScan)
                    }
                ) { Text(scanningText) }

                Spacer(Modifier.width(8.dp))

                Button(onClick = { onInput(BluetoothInput.AutoConnect) }) {
                    Text("자동연결")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth()) {
            items(uiState.scanResults) { scan ->
                ScanTabItem(
                    name = scan.name ?: "Unknown",
                    onConnect = {
                        val device = BluetoothAdapter.getDefaultAdapter()
                            .getRemoteDevice(scan.address)
                        onInput(BluetoothInput.Connect(device))
                    }
                )
            }
        }
    }
}