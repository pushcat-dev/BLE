package com.example.blecompose.core.bluetooth.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.os.ParcelUuid
import com.example.blecompose.core.bluetooth.model.TapScanResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.android.support.v18.scanner.*
import timber.log.Timber
import javax.inject.Inject

class TapScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val scanner = BluetoothLeScannerCompat.getScanner()
    private var isScanning = false

    private var savedCallback: ScanCallback? = null

    private val MOTION_TAP_SERVICE_UUID =
        ParcelUuid.fromString("0000180A-0000-1000-8000-00805F9B34FB")

    private val MOTION_TAP_NAME = "Motion Tap"

    @SuppressLint("MissingPermission")
    fun startScan(): Flow<List<TapScanResult>> = callbackFlow {

        if (isScanning) {
            trySend(emptyList())
            return@callbackFlow
        }
        isScanning = true

        val scanned = LinkedHashMap<String, TapScanResult>()

        val callback = object : ScanCallback() {

            override fun onScanResult(type: Int, result: ScanResult) {
                if (isMotionTapDevice(result)) {
                    handleResult(result, scanned)
                    trySend(scanned.values.toList())
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.forEach { r ->
                    if (isMotionTapDevice(r)) {
                        handleResult(r, scanned)
                    }
                }
                trySend(scanned.values.toList())
            }

            override fun onScanFailed(errorCode: Int) {
                Timber.e("Scan failed: $errorCode")
                close()
            }
        }

        savedCallback = callback

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .setUseHardwareBatchingIfSupported(false)
            .build()

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(MOTION_TAP_SERVICE_UUID)
                .setDeviceName(MOTION_TAP_NAME)
                .build()
        )

        scanner.startScan(filters, settings, callback)

        awaitClose { stopScan() }
    }

    private fun isMotionTapDevice(result: ScanResult): Boolean {
        val name = result.scanRecord?.deviceName ?: return false
        return name.equals(MOTION_TAP_NAME, ignoreCase = true)
    }

    private fun handleResult(
        result: ScanResult,
        map: LinkedHashMap<String, TapScanResult>
    ) {
        val device = result.device ?: return
        val address = device.address ?: return

        val record = result.scanRecord

        val name = record?.deviceName ?: "Unknown"
        val manufacturer = record?.manufacturerSpecificData?.let {
            if (it.size() > 0) it.valueAt(0) else null
        }

        map[address] = TapScanResult(
            name = name,
            address = address,
            rssi = result.rssi,
            isConnectable = result.isConnectable,
            manufacturerData = manufacturer
        )
    }

    fun stopScan() {
        val cb = savedCallback ?: return
        try {
            scanner.stopScan(cb)
        } catch (_: Exception) {
        }
        savedCallback = null
        isScanning = false
    }

    companion object {
        fun checkLocationEnabled(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return true
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

    }
}