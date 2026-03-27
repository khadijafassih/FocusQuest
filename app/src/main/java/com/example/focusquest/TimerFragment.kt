package com.example.focusquest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.focusquest.databinding.FragmentTimerBinding

class TimerFragment : Fragment() {
    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 1500000 // 25 minutes
    private var timerRunning = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStartPause.setOnClickListener {
            if (timerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        binding.btnReset.setOnClickListener {
            resetTimer()
        }

        updateCountDownText()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
                updateProgressBar()
            }

            override fun onFinish() {
                timerRunning = false
                binding.btnStartPause.text = "Start"
                addXP(20)
                sendNotification()
                Toast.makeText(context, "Focus session complete! +20 XP 🎉", Toast.LENGTH_LONG).show()
                resetTimer()
            }
        }.start()

        timerRunning = true
        binding.btnStartPause.text = "Pause"
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        binding.btnStartPause.text = "Start"
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        timeLeftInMillis = 1500000
        updateCountDownText()
        updateProgressBar()
        binding.btnStartPause.text = "Start"
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        binding.tvTimer.text = timeLeftFormatted
    }

    private fun updateProgressBar() {
        binding.pbTimer.progress = (timeLeftInMillis / 1000).toInt()
    }

    private fun addXP(amount: Int) {
        val prefs = requireContext().getSharedPreferences("FocusQuestPrefs", Context.MODE_PRIVATE)
        val currentXP = prefs.getInt("XP", 0)
        prefs.edit().putInt("XP", currentXP + amount).apply()
    }

    private fun sendNotification() {
        val context = requireContext()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "focus_quest_timer"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Timer Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Focus Session Complete!")
            .setContentText("Well done! You earned +20 XP.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
        
        // Broadcast for potential receiver usage as requested
        val intent = Intent("com.example.focusquest.TIMER_FINISHED")
        context.sendBroadcast(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}
