package com.example.blecompose.core.bluetooth.constant

object GameCommand {

    const val DEFAULT_VALUE = "00"
    const val GAME_READY_CODE = "1B51"
    const val LED_CODE = "1B61"

    const val BLINKY_LED_CODE = "1B61000712"
    const val LED_OFF_CODE = "1B61000000"

    const val GAME_END_CODE = "1B59"
    const val GAME_START_CMD_CODE = "1B53"

    const val GAME_START_CMD_LED = "1B5300"

    const val GAME_START_RED_LED_CODE = "1B530001"
    const val GAME_START_GREEN_LED_CODE = "1B530002"
    const val GAME_START_BLUE_LED_CODE = "1B530003"
    const val GAME_START_RG_LED_CODE = "1B530004"
    const val GAME_START_RB_LED_CODE = "1B530005"
    const val GAME_START_GB_LED_CODE = "1B530006"
    const val GAME_START_RGB_LED_CODE = "1B530007"

    const val GAME_TAP_LED_OFF_CODE = "1B530000"
    const val GAME_TAP_LED_CONNECT_CODE = "0232000000"
    const val GAME_TAP_LED_CONNECT_CODE_NO_COLOR = "A5"

    const val TAP_BATTERY_CODE = "1B41"

    const val GAME_REPLY_SENSING = "54"
    const val GAME_REPLY_TIMEOUT = "55"
    const val GAME_REPLY_LEDOFF = "61"

    const val COLOR_RED = "01"
    const val COLOR_GRN = "02"
    const val COLOR_BLU = "03"
    const val COLOR_YEL = "04"
    const val COLOR_MAG = "05"
    const val COLOR_CYN = "06"
    const val COLOR_WHT = "07"

    const val INFINITY_BLINK = "50"

    const val GAME_READY_SINGLE_LED_ON = LED_CODE + DEFAULT_VALUE + COLOR_BLU + INFINITY_BLINK
}