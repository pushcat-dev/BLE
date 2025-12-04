package com.example.blecompose.feature.bluetooth.presentation.input

import android.bluetooth.BluetoothDevice



/**
 * UI → ViewModel 로 전달되는 모든 BLE 관련 이벤트 정의
 */
sealed class BluetoothInput {

    // -------------------------------------------------------
    // Scan 관련
    // -------------------------------------------------------

    /** 스캔 시작 요청 */
    object StartScan : BluetoothInput()

    /** 스캔 중지 요청 */
    object StopScan : BluetoothInput()


    // -------------------------------------------------------
    // 연결 / 연결해제
    // -------------------------------------------------------

    /**
     * 특정 BLE 디바이스 연결 요청
     */
    data class Connect(val device: BluetoothDevice) : BluetoothInput()

    /**
     * 특정 BLE 디바이스 연결 해제 요청
     */
    data class Disconnect(val device: BluetoothDevice) : BluetoothInput()

    /**
     * 연결된 모든 디바이스 연결 해제 요청
     */
    object DisconnectAll : BluetoothInput()


    // -------------------------------------------------------
    // 송신(Write)
    // -------------------------------------------------------

    /**
     * 특정 디바이스에 raw byte array 전송
     */
    data class Send(val address: String, val bytes: ByteArray) : BluetoothInput()

    /**
     * 연결된 모든 디바이스에 raw byte array 전송
     */
    data class SendAll(val bytes: ByteArray) : BluetoothInput()


    // -------------------------------------------------------
    // LED 제어 (개별 탭 점등)
    // -------------------------------------------------------

    /**
     * 특정 디바이스 LED 켜기 요청
     */
    data class LightOn(val address: String) : BluetoothInput()

    /**
     * 특정 디바이스 LED 끄기 요청
     */
    data class LightOff(val address: String) : BluetoothInput()


    // -------------------------------------------------------
    // 자동 연결 & 서비스 시작
    // -------------------------------------------------------

    /**
     * 스캔 결과 중 조건 맞는 디바이스 자동 연결
     */
    object AutoConnect : BluetoothInput()

    /**
     * Foreground BLE Service 시작 요청
     */
    object StartService : BluetoothInput()
}