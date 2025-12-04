package com.example.blecompose.feature.bluetooth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.bluetooth.BluetoothDevice
import com.example.blecompose.feature.bluetooth.domain.useCase.*
import com.example.blecompose.feature.bluetooth.presentation.input.BluetoothInput
import com.example.blecompose.feature.bluetooth.presentation.output.BluetoothUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val startScanUseCase: StartScanUseCase,
    private val stopScanUseCase: StopScanUseCase,
    private val connectUseCase: ConnectUseCase,
    private val disconnectUseCase: DisconnectUseCase,
    private val disconnectAllUseCase: DisconnectAllUseCase,
    private val getConnectedDevicesUseCase: GetConnectedDevicesUseCase,
    private val mergedNotifyUseCase: MergedNotifyUseCase,
    private val sendUseCase: SendUseCase,
    private val sendAllUseCase: SendAllUseCase,
    private val startBleServiceUseCase: StartBleServiceUseCase,
    private val autoConnectUseCase: AutoConnectUseCase,
    private val lightEachUseCase: LightEachUseCase      //  LED ON/OFF UseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState: StateFlow<BluetoothUiState> = _uiState

    init {
        observeConnected()
        observeNotify()
    }

    fun process(input: BluetoothInput) {
        when (input) {
            is BluetoothInput.StartScan -> startScan()
            is BluetoothInput.StopScan -> stopScan()

            is BluetoothInput.Connect -> connect(input.device)
            is BluetoothInput.Disconnect -> disconnect(input.device)
            is BluetoothInput.DisconnectAll -> disconnectAll()

            is BluetoothInput.Send -> send(input.address, input.bytes)
            is BluetoothInput.SendAll -> sendAll(input.bytes)

            is BluetoothInput.LightOn -> light(input.address, true)    //  ON
            is BluetoothInput.LightOff -> light(input.address, false) //  OFF

            is BluetoothInput.AutoConnect -> autoConnect()
            is BluetoothInput.StartService -> startService()
        }
    }

    // -------------------------------------------------------
    // LED ON/OFF
    // -------------------------------------------------------
    private fun light(address: String, turnOn: Boolean) {
        lightEachUseCase(address, turnOn)

        _uiState.update {
            it.copy(
                lightStates = it.lightStates + (address to turnOn)
            )
        }
    }

    // -------------------------------------------------------
    // Scan
    // -------------------------------------------------------

    private fun startScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(scanning = true) }

            startScanUseCase().collect { list ->
                val connectedAddresses = uiState.value.connectedDevices.map { it.address }.toSet()

                val filtered = list
                    .filter { it.address !in connectedAddresses }
                    .sortedByDescending { it.rssi }

                _uiState.update { it.copy(scanResults = filtered) }
            }
        }
    }

    private fun stopScan() {
        stopScanUseCase()
        _uiState.update { it.copy(scanning = false) }
    }

    // -------------------------------------------------------
    // Connection
    // -------------------------------------------------------

    private fun connect(device: BluetoothDevice) {
        connectUseCase(device)
    }

    private fun disconnect(device: BluetoothDevice) {
        disconnectUseCase(device)
    }

    private fun disconnectAll() {
        disconnectAllUseCase()
    }

    private fun autoConnect() {
        val list = uiState.value.scanResults
        autoConnectUseCase(list)
    }

    // -------------------------------------------------------
    // Connected Observer
    // -------------------------------------------------------

    private fun observeConnected() {
        viewModelScope.launch {
            getConnectedDevicesUseCase().collect { list ->
                _uiState.update { it.copy(connectedDevices = list) }
            }
        }
    }

    // -------------------------------------------------------
    // Notify
    // -------------------------------------------------------

    private fun observeNotify() {
        viewModelScope.launch {
            mergedNotifyUseCase().collect { (addr, data) ->
                _uiState.update {
                    it.copy(
                        notifyLogs = (it.notifyLogs + (addr to data)).takeLast(50)
                    )
                }
            }
        }
    }

    // -------------------------------------------------------
    // Write Raw
    // -------------------------------------------------------

    private fun send(address: String, bytes: ByteArray) =
        sendUseCase(address, bytes)

    private fun sendAll(bytes: ByteArray) =
        sendAllUseCase(bytes)

    // -------------------------------------------------------
    // Service
    // -------------------------------------------------------

    private fun startService() = startBleServiceUseCase()
}