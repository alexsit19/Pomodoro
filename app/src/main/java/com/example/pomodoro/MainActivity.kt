package com.example.pomodoro

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding
import android.os.CountDownTimer as CountDownTimer

class MainActivity : AppCompatActivity(), TimerListener, LifecycleObserver {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val timers = mutableListOf<Timer>()
    private var nextId = 0
    private val pomodoroAdapter = PomodoroAdapter(this)
    private var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startTime = System.currentTimeMillis()

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pomodoroAdapter

        }

        binding.addTimerBtn.setOnClickListener{
            val text = binding.timerValueEt.text.toString()
            val number = text.toIntOrNull()

            if(number == null) {
                Toast.makeText(this,
                    "в поле ввода значение отсутствует или превышает допустимый уровень",
                     Toast.LENGTH_SHORT).show()
            } else if (number == 0) {
                Toast.makeText(this,
                    "значение равно нулю, нечего засекать",
                    Toast.LENGTH_SHORT).show()
            } else {
                val hour = text.toInt() / 60

                if (hour >= 24) {
                    Toast.makeText(this,
                        "значение часов больше 23 часов, 59 минут",
                        Toast.LENGTH_SHORT).show()
                } else {
                    val currentTime = text.toLong() * 1000 * 60
                    timers.add(Timer(
                            nextId++,
                            currentTime = currentTime,
                            isStarted = false,
                            isOver = false
                            ))

                    pomodoroAdapter.submitList(timers.toList())
                    pomodoroAdapter.notifyDataSetChanged()
                    }
            }
            //binding.timerValueEt.setText("")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startedTimer: Timer? = getStartedTimer()

        if (startedTimer != null) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, startTime)
            startIntent.putExtra(CURRENT_TIME, startedTimer?.currentTime)
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    override fun onBackPressed() {
        val startedTimer: Timer? = getStartedTimer()
        if (startedTimer != null) {
            this.moveTaskToBack(true)
        } else {
            finish()
        }
    }

    private fun getStartedTimer(): Timer? {
        var startedTimer: Timer? = null
        for (timer in timers) {
            if (timer.isStarted) {
                startedTimer = timer
            }
        }
        return startedTimer
    }

    companion object {
        private const val INTERVAL = 100L
        private const val PERIOD = 1000L * 30 // 30 sec
    }

    override fun start(id: Int, currentMs: Long) {
        changeTimer(id, currentMs, true)
        pomodoroAdapter.notifyDataSetChanged()

    }

    override fun stop(id: Int, currentMs: Long) {
        changeTimer(id, currentMs, false)
        pomodoroAdapter.notifyDataSetChanged()
    }

    override fun reset(id: Int) {
        val timer = timers.find { it.id == id }
        timer?.let {
            it.currentTime = it.totalMs
            it.isOver = false
        }
        pomodoroAdapter.submitList(timers)
        pomodoroAdapter.notifyDataSetChanged()
    }

    override fun delete(id: Int) {
        timers.remove( timers.find { it.id == id } )
        pomodoroAdapter.submitList(timers)
        pomodoroAdapter.notifyDataSetChanged()
    }

    private fun changeTimer(id: Int, currentMs: Long, isStarted: Boolean) {
        val newTimers = mutableListOf<Timer>()

        timers.forEach {
            if (it.id == id) {
                newTimers.add(Timer(it.id, currentMs, isStarted, it.totalMs, it.isOver))
            } else {
                it.isStarted = false
                newTimers.add(it)
            }
        }

        pomodoroAdapter.submitList(newTimers)
        timers.clear()
        timers.addAll(newTimers)
    }
}

