package com.example.pomodoro

const val START_TIME = "00:00:00"
const val COMMAND_START = "COMMAND_START"
const val COMMAND_STOP = "COMMAND_STOP"
const val COMMAND_ID = "COMMAND_ID"
const val INVALID = "INVALID"
const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME"
const val CURRENT_TIME = "CURRENT_TIME"

fun Long.displayTime(): String {
    var hour = 0L
    var minute = 0L
    var second = 0L

    if (this <= 0L) {
        return START_TIME

    } else {
        second = this / 1000 % 60
        minute = this / 1000 % 3600 / 60
        hour = this / 1000 / 3600

    }

    return String.format("%02d:%02d:%02d", hour, minute, second)
}
