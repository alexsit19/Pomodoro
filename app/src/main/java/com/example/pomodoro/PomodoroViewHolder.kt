package com.example.pomodoro

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.media.RingtoneManager
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.TimerItemBinding

class PomodoroViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val context: Context,
): RecyclerView.ViewHolder(binding.root) {

    private var countDownTimer: CountDownTimer? = null
    private var totalMs = 0L

    fun bind(timer: Timer) {
        binding.timerTv.text = timer.currentTime.displayTime()
        totalMs = timer.totalMs
        if (timer.isOver) {
            binding.startStopBtn.isEnabled = false
            binding.progressImage.setCurrent(timer.totalMs)
            binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.light_salmon))
            binding.refreshBtn.background = ContextCompat.getColor(context, R.color.light_salmon).toDrawable()
            binding.deleteButton.setBackgroundColor(ContextCompat.getColor(context, R.color.light_salmon))
        } else {
            binding.startStopBtn.isEnabled = true
            binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            binding.refreshBtn.background = ContextCompat.getColor(context, R.color.white).toDrawable()
            binding.deleteButton.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            binding.progressImage.setCurrent(0L)
        }

        if (timer.isStarted) {
            startTimer(timer)
            setIsRecyclable(false)
        } else {
            stopTimer(timer)
            setIsRecyclable(true)
        }

        addButtonsListener(timer)
    }

    private fun startTimer(timer: Timer) {
        binding.progressImage.setPeriod(totalMs)
        binding.progressImage.setCurrent(totalMs - timer.currentTime)
        binding.startStopBtn.text = "STOP"
        countDownTimer?.cancel()
        countDownTimer = getCountDownTimer(timer)
        countDownTimer?.start()
        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()

    }

    private fun stopTimer(timer: Timer) {
        binding.progressImage.setPeriod(totalMs)
        binding.progressImage.setCurrent(totalMs - timer.currentTime)
        binding.startStopBtn.text = "START"
        countDownTimer?.cancel()
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator as? AnimationDrawable)?.stop()

    }

    private fun getCountDownTimer(timer: Timer): CountDownTimer {
        return object : CountDownTimer(timer.currentTime, INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {

                if(timer.isStarted) {
                    binding.timerTv.text = millisUntilFinished.displayTime()
                    timer.currentTime = millisUntilFinished
                    binding.progressImage.setCurrent(totalMs - timer.currentTime)
                }
            }

            override fun onFinish() {
                binding.startStopBtn.text = "START"
                binding.startStopBtn.isEnabled = false
                binding.blinkingIndicator.isInvisible = true
                binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.light_salmon))
                binding.refreshBtn.background =
                    ContextCompat.getColor(context, R.color.light_salmon).toDrawable()
                binding.deleteButton.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_salmon
                    )
                )
                (binding.blinkingIndicator as? AnimationDrawable)?.stop()
                if (timer.currentTime < 1000L) {
                    timer.isStarted = false
                    timer.currentTime = timer.totalMs
                    binding.progressImage.setCurrent(0)
                    binding.timerTv.text = timer.totalMs.displayTime()
                    if (!timer.isStarted && !timer.isOver) {
                        timer.isOver = true
                        startAlarm()
                        Toast.makeText(
                            context,
                            "Время вышло",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun addButtonsListener(timer: Timer) {
        binding.startStopBtn.setOnClickListener {
            countDownTimer?.cancel()
            if (timer.isStarted) {

                listener.stop(timer.id, timer.currentTime)

            } else {

                listener.start(timer.id, timer.currentTime)
            }
        }

        binding.deleteButton.setOnClickListener {
            countDownTimer?.cancel()
            timer.isStarted = false
            setIsRecyclable(true)
            listener.delete(timer.id)
        }

        binding.refreshBtn.setOnClickListener {
            timer.isStarted = false
            setIsRecyclable(true)
            listener.reset(timer.id)
        }
    }

    private fun startAlarm() {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val alarm = RingtoneManager.getRingtone(context, notification)
        alarm.play()
    }

    private companion object {
        private const val INTERVAL = 1000L
    }
}