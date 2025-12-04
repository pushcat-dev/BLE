package com.example.blecompose

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.blecompose.feature.bluetooth.presentation.screen.BleScreen
import com.example.blecompose.feature.bluetooth.presentation.viewmodel.BluetoothViewModel
import com.example.blecompose.feature.bluetooth.domain.repository.BluetoothRepository
import com.example.blecompose.core.bluetooth.service.TapBleService
import com.example.blecompose.ui.theme.BLEComposeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: BluetoothRepository

    private var bleService: TapBleService? = null

    private val permissions = arrayOf(
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    // -------------------------------------------------------
    // ServiceConnection
    // -------------------------------------------------------
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service = (binder as TapBleService.LocalBinder).getService()
            bleService = service
            repository.bindService(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            repository.unbindService()
            bleService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasPermissions()) {
            requestPermissions(permissions, 100)
            return
        }

        startAndBindService()
        setupUI()
    }

    private fun hasPermissions(): Boolean =
        permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    private fun startAndBindService() {
        val intent = Intent(this, TapBleService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun setupUI() {
        enableEdgeToEdge()

        setContent {
            BLEComposeTheme {
                val vm: BluetoothViewModel = hiltViewModel()
                val uiState = vm.uiState.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    BleScreen(
                        uiState = uiState.value,
                        onInput = { vm.process(it) },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }

    // -------------------------------------------------------
    // Permission callback
    // -------------------------------------------------------
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 && hasPermissions()) {
            startAndBindService()
            setupUI()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}