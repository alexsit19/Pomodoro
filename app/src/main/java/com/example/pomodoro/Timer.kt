package com.example.pomodoro

import android.os.CountDownTimer

data class Timer (
    val id: Int,
    var currentTime: Long,
    var isStarted: Boolean,
    val totalMs: Long = currentTime,
    var isOver: Boolean,
    //var cdTimer: CountDownTimer?
    )
