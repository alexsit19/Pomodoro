package com.example.pomodoro

import android.os.CountDownTimer

interface TimerListener {

    fun start(id: Int, currentMs: Long)

    fun stop(id: Int, currentMs: Long)

    fun reset(id: Int)

    fun delete(id: Int)

}