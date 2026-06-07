package com.example.focusquest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.focusquest.data.db.AppDatabase
import com.example.focusquest.data.db.entity.FocusSessionEntity
import com.example.focusquest.viewmodel.SessionType
import com.example.focusquest.viewmodel.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimerService : Service() {

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    private val binder = TimerBinder()

    companion object {
        const val CHANNEL_ID = "focusquest_timer_svc"
        const val NOTIF_ID   = 42
        const val ACTION_PAUSE = "com.example.focusquest.PAUSE"
        const val ACTION_RESET = "com.example.focusquest.RESET"
    }

    var sessionType: SessionType = SessionType.POMODORO; private set
    var timerState: TimerState   = TimerState.IDLE;      private set
    var timeRemainingMs: Long    = SessionType.POMODORO.minutes * 60_000L; private set
    var totalMs: Long            = SessionType.POMODORO.minutes * 60_000L; private set

    var linkedTaskId:    Long?   = null
    var linkedTaskTitle: String? = null

    var onTick:        ((Long, Int) -> Unit)?                                              = null
    var onFinish:      ((xpEarned: Int, achievements: List<AchievementManager.AchievementDef>) -> Unit)? = null
    var onStateChange: ((TimerState) -> Unit)?                                             = null

    private val serviceJob   = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var timer:       CountDownTimer? = null
    private val soundPlayer  = SoundPlayer()
    var selectedSoundName: String? = null

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        soundPlayer.init(this)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESET -> { resetTimer(); stopSelf() }
        }
        return START_STICKY
    }

    fun selectType(type: SessionType, customMinutes: Int? = null) {
        if (timerState == TimerState.IDLE) {
            sessionType = type
            val min = customMinutes ?: type.minutes
            totalMs = min * 60_000L
            timeRemainingMs = totalMs
        }
    }

    fun startTimer() {
        if (timerState == TimerState.RUNNING) return
        val remaining = timeRemainingMs
        timer = object : CountDownTimer(remaining, 500) {
            override fun onTick(ms: Long) {
                timeRemainingMs = ms
                val pct = ((totalMs - ms) * 100 / totalMs).toInt()
                onTick?.invoke(ms, pct)
                updateNotif(ms)
            }
            override fun onFinish() {
                timeRemainingMs = 0L
                timerState = TimerState.FINISHED
                onStateChange?.invoke(TimerState.FINISHED)
                soundPlayer.stop()
                updateNotif(0L)
                saveSession()
            }
        }.start()
        timerState = TimerState.RUNNING
        onStateChange?.invoke(TimerState.RUNNING)
        startForeground(NOTIF_ID, buildNotif(timeRemainingMs))
        selectedSoundName?.let { soundPlayer.resume() }
    }

    fun pauseTimer() {
        timer?.cancel()
        timerState = TimerState.PAUSED
        onStateChange?.invoke(TimerState.PAUSED)
        soundPlayer.pause()
        updateNotif(timeRemainingMs)
    }

    fun resetTimer() {
        timer?.cancel()
        timeRemainingMs = totalMs
        timerState = TimerState.IDLE
        onStateChange?.invoke(TimerState.IDLE)
        soundPlayer.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun setSound(name: String?) {
        selectedSoundName = name
        when {
            name == null -> soundPlayer.stop()
            timerState == TimerState.RUNNING -> soundPlayer.play(name)
            timerState == TimerState.PAUSED  -> { /* will resume on next startTimer */ }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "Focus Timer", NotificationManager.IMPORTANCE_LOW)
                .apply { setSound(null, null); enableVibration(false) }
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotif(ms: Long): android.app.Notification {
        val min = (ms / 1000) / 60; val sec = (ms / 1000) % 60
        val timeStr = String.format("%02d:%02d", min, sec)
        val text = when (timerState) {
            TimerState.RUNNING  -> "Focusing — $timeStr left"
            TimerState.PAUSED   -> "Paused — $timeStr left"
            TimerState.FINISHED -> "Session complete!"
            else -> sessionType.label
        }
        val mainPi  = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val pausePi = PendingIntent.getService(this, 1,
            Intent(this, TimerService::class.java).apply { action = ACTION_PAUSE },
            PendingIntent.FLAG_IMMUTABLE)
        val resetPi = PendingIntent.getService(this, 2,
            Intent(this, TimerService::class.java).apply { action = ACTION_RESET },
            PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notif)
            .setContentTitle("FocusQuest — ${sessionType.label}")
            .setContentText(text)
            .setContentIntent(mainPi)
            .setOngoing(timerState == TimerState.RUNNING || timerState == TimerState.PAUSED)
            .setSilent(true)
            .addAction(R.drawable.ic_pause, "Pause", pausePi)
            .addAction(R.drawable.ic_reset, "Reset", resetPi)
            .build()
    }

    private fun updateNotif(ms: Long) {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIF_ID, buildNotif(ms))
    }

    private fun saveSession() {
        val capturedType       = sessionType
        val capturedTotalMs    = totalMs
        val capturedTaskId     = linkedTaskId
        val capturedTaskTitle  = linkedTaskTitle

        serviceScope.launch {
            val prefs    = UserPreferencesManager(this@TimerService)
            val username = prefs.getCurrentUser()?.username
            if (username == null) {
                withContext(Dispatchers.Main) { onFinish?.invoke(0, emptyList()) }
                return@launch
            }

            val sType = when (capturedType) {
                SessionType.SHORT_BREAK -> FocusSessionEntity.TYPE_SHORT_BREAK
                SessionType.LONG_BREAK  -> FocusSessionEntity.TYPE_LONG_BREAK
                else                    -> FocusSessionEntity.TYPE_FOCUS
            }

            val db = AppDatabase.getInstance(this@TimerService)
            db.focusSessionDao().insert(
                FocusSessionEntity(
                    username      = username,
                    taskId        = capturedTaskId,
                    taskTitle     = capturedTaskTitle,
                    durationMinutes = (capturedTotalMs / 60_000L).toInt(),
                    sessionType   = sType
                )
            )

            var xpEarned    = 0
            var achievements = emptyList<AchievementManager.AchievementDef>()
            if (sType == FocusSessionEntity.TYPE_FOCUS) {
                prefs.updateUserXP(capturedType.xp)
                xpEarned     = capturedType.xp
                achievements = AchievementManager.checkAndUnlock(username, db)
            }

            withContext(Dispatchers.Main) {
                onFinish?.invoke(xpEarned, achievements)
            }
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        soundPlayer.stop()
        serviceJob.cancel()
        super.onDestroy()
    }
}
