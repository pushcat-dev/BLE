package com.example.blecompose.feature.bluetooth.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blecompose.feature.bluetooth.domain.entity.DomainTapScanResult

@Composable
fun AvailableTapItem(
    tap: DomainTapScanResult,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Bluetooth, contentDescription = null)
            Text(tap.name ?: "Unknown")
            Text(tap.address, style = MaterialTheme.typography.bodySmall)

            Button(onClick = onConnect, modifier = Modifier.fillMaxWidth()) {
                Text("연결")
            }
        }
    }
}