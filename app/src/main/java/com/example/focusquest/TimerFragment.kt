package com.example.focusquest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.focusquest.data.db.AppDatabase
import com.example.focusquest.data.db.entity.TaskEntity
import com.example.focusquest.databinding.FragmentTimerBinding
import com.example.focusquest.viewmodel.SessionType
import com.example.focusquest.viewmodel.TimerState
import com.example.focusquest.viewmodel.TimerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private val vm: TimerViewModel by viewModels()
    private var pendingTasks = listOf<TaskEntity>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSessionChips()
        setupSoundChips()
        setupTaskSelector()
        setupControls()
        observeViewModel()
        loadPendingTasks()
    }

    private fun setupSessionChips() {
        binding.chipPomodoro.setOnClickListener { vm.selectType(SessionType.POMODORO) }
        binding.chipDeepFocus.setOnClickListener { vm.selectType(SessionType.DEEP_FOCUS) }
        binding.chipUltraFocus.setOnClickListener { vm.selectType(SessionType.ULTRA_FOCUS) }
    }

    private fun setupSoundChips() {
        binding.chipSoundNone.setOnClickListener       { vm.setSound(null) }
        binding.chipSoundBrownNoise.setOnClickListener { vm.setSound(SoundPlayer.BROWN_NOISE) }
        binding.chipSoundRain.setOnClickListener       { vm.setSound(SoundPlayer.RAIN) }
        binding.chipSoundLofi.setOnClickListener       { vm.setSound(SoundPlayer.LOFI) }
        binding.chipSoundWhiteNoise.setOnClickListener { vm.setSound(SoundPlayer.WHITE_NOISE) }
    }

    private fun setupTaskSelector() {
        binding.btnSelectTask.setOnClickListener {
            if (pendingTasks.isEmpty()) {
                Toast.makeText(context, "No pending tasks. Add one in the Tasks tab!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val items = arrayOf("None") + pendingTasks.map { it.title }.toTypedArray()
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Link a task")
                .setItems(items) { _, which ->
                    if (which == 0) {
                        vm.setLinkedTask(null)
                        binding.tvLinkedTaskName.text = "None selected"
                    } else {
                        val task = pendingTasks[which - 1]
                        vm.setLinkedTask(task)
                        binding.tvLinkedTaskName.text = task.title
                    }
                }.show()
        }
    }

    private fun setupControls() {
        binding.btnStartPause.setOnClickListener { vm.toggleStartPause() }
        binding.btnReset.setOnClickListener { vm.reset() }
        binding.btnShortBreak.setOnClickListener {
            vm.reset()
            vm.selectType(SessionType.SHORT_BREAK)
            vm.toggleStartPause()
            binding.cardBreak.visibility = View.GONE
        }
        binding.btnLongBreak.setOnClickListener {
            vm.reset()
            vm.selectType(SessionType.LONG_BREAK)
            vm.toggleStartPause()
            binding.cardBreak.visibility = View.GONE
        }
        binding.btnDismissBreak.setOnClickListener { binding.cardBreak.visibility = View.GONE }
    }

    private fun observeViewModel() {
        vm.timerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                TimerState.RUNNING -> {
                    binding.btnStartPause.text = "Pause"
                    binding.btnStartPause.setIconResource(R.drawable.ic_pause)
                }
                TimerState.PAUSED -> {
                    binding.btnStartPause.text = "Resume"
                    binding.btnStartPause.setIconResource(R.drawable.ic_play)
                }
                TimerState.FINISHED -> {
                    binding.btnStartPause.text = "Start"
                    binding.btnStartPause.setIconResource(R.drawable.ic_play)
                }
                else -> {
                    binding.btnStartPause.text = "Start"
                    binding.btnStartPause.setIconResource(R.drawable.ic_play)
                }
            }
        }

        vm.timeRemainingMs.observe(viewLifecycleOwner) { ms ->
            val min = (ms / 1000) / 60
            val sec = (ms / 1000) % 60
            binding.tvTimer.text = String.format("%02d:%02d", min, sec)
        }

        vm.progressPercent.observe(viewLifecycleOwner) { progress ->
            binding.circularProgress.progress = progress
        }

        vm.sessionType.observe(viewLifecycleOwner) { type ->
            binding.chipPomodoro.isChecked = type == SessionType.POMODORO
            binding.chipDeepFocus.isChecked = type == SessionType.DEEP_FOCUS
            binding.chipUltraFocus.isChecked = type == SessionType.ULTRA_FOCUS
            binding.tvXpReward.text = if (type.xp > 0) "+${type.xp} XP" else "Break Time"
        }

        vm.completedSessionsToday.observe(viewLifecycleOwner) { count ->
            binding.tvSessionsToday.text = "$count sessions today"
        }

        vm.sessionCompleted.observe(viewLifecycleOwner) { done ->
            if (done) {
                val xp      = vm.lastXpEarned.value ?: 0
                val isFocus = xp > 0
                if (isFocus) {
                    Toast.makeText(context, "Session complete! +$xp XP earned!", Toast.LENGTH_LONG).show()
                    sendNotification(xp)
                    triggerHaptic()
                    binding.cardBreak.visibility = View.VISIBLE
                } else {
                    Toast.makeText(context, "Break over! Time to focus.", Toast.LENGTH_SHORT).show()
                }
                vm.acknowledgeCompletion()
            }
        }

        vm.newAchievements.observe(viewLifecycleOwner) { achievements ->
            if (achievements.isNotEmpty()) {
                showAchievementDialog(achievements)
                vm.acknowledgeAchievements()
            }
        }
    }

    private fun triggerHaptic() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(400)
        }
    }

    private fun showAchievementDialog(achievements: List<AchievementManager.AchievementDef>) {
        val ctx = context ?: return
        val names = achievements.joinToString("\n") { "${it.emoji} ${it.title}" }
        androidx.appcompat.app.AlertDialog.Builder(ctx)
            .setTitle("Achievement Unlocked!")
            .setMessage(names)
            .setPositiveButton("Awesome!", null)
            .show()
    }

    private fun loadPendingTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            val prefs = UserPreferencesManager(requireContext())
            val username = prefs.getCurrentUser()?.username ?: return@launch
            pendingTasks = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(requireContext()).taskDao().getPendingTasksSync(username)
            }
        }
    }

    private fun sendNotification(xp: Int) {
        val ctx = context ?: return
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "focusquest_timer"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Focus Timer", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        nm.notify(101, NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.ic_timer_notif)
            .setContentTitle("Focus Session Complete!")
            .setContentText("Great work! You earned +$xp XP.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
