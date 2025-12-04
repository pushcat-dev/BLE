package com.example.blecompose.feature.bluetooth.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConnectedTabItem(
    name: String,
    isLightOn: Boolean,
    onLightOn: () -> Unit,
    onLightOff: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        Modifier
            .size(120.dp)
            .padding(8.dp)
    ) {
        Column(
            Modifier.padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name)

            Row {
                // ---------------------------
                // LED On/Off UI 구분
                // ---------------------------
                if (isLightOn) {
                    Button(
                        onClick = onLightOff,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("끄기")
                    }
                } else {
                    Button(
                        onClick = onLightOn,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("켜기")
                    }
                }

                Spacer(Modifier.width(8.dp))

                Button(onClick = onDisconnect, Modifier.weight(1f)) {
                    Text("해제")
                }
            }
        }
    }
}