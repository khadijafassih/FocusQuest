package com.example.focusquest

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.focusquest.databinding.FragmentProfileBinding
import com.example.focusquest.viewmodel.SessionType
import com.example.focusquest.viewmodel.StatsViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val statsVm: StatsViewModel by viewModels()
    private lateinit var prefs: UserPreferencesManager

    // Per-type mutable durations (edited locally, saved on change)
    private var pomodoroMin = 25
    private var deepFocusMin = 50
    private var ultraFocusMin = 90

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                binding.ivProfile.setImageURI(it)
                prefs.updateProfileImage(it.toString())
            } catch (_: Exception) {
                binding.ivProfile.setImageURI(it)
                prefs.updateProfileImage(it.toString())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = UserPreferencesManager(requireContext())

        loadProfile()
        observeStats()
        setupButtons()
        setupDarkModeChips()
        setupReminderSection()
        setupTimerDurationSection()
    }

    private fun loadProfile() {
        val user = prefs.getCurrentUser() ?: return
        binding.tvProfileName.text = user.username
        binding.tvProfileEmail.text = user.email
        binding.tvLevel.text = "Level ${user.level}"
        val xpInLevel = user.xp % 100
        binding.pbXP.progress = xpInLevel
        binding.tvXpProgress.text = "$xpInLevel / 100 XP"
        binding.tvTotalXP.text = "${user.xp} Total XP"

        val initials = user.username.take(2).uppercase()
        binding.tvAvatarInitials.text = initials

        user.profileImage?.let { uriStr ->
            try {
                binding.ivProfile.setImageURI(Uri.parse(uriStr))
                binding.tvAvatarInitials.visibility = View.GONE
            } catch (_: Exception) {
                prefs.clearCurrentUserProfileImage()
            }
        }
    }

    private fun observeStats() {
        statsVm.totalSessions.observe(viewLifecycleOwner) { binding.tvTotalSessions.text = it.toString() }
        statsVm.totalHours.observe(viewLifecycleOwner) { binding.tvTotalHours.text = String.format("%.1f", it) }
        statsVm.totalTasksCompleted.observe(viewLifecycleOwner) { binding.tvTotalTasks.text = it.toString() }
        statsVm.currentStreak.observe(viewLifecycleOwner) { binding.tvStreak.text = it.toString() }
    }

    private fun setupButtons() {
        binding.ivProfile.setOnClickListener { pickImage.launch("image/*") }
        binding.btnChangePhoto.setOnClickListener { pickImage.launch("image/*") }

        binding.btnViewStats.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, StatsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnAchievements.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AchievementsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnResetStats.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reset Progression")
                .setMessage("This will reset your XP and level. Your tasks will stay. Continue?")
                .setPositiveButton("Reset") { _, _ ->
                    prefs.setCurrentUserXP(0)
                    loadProfile()
                    Toast.makeText(context, "Progression reset", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnLogout.setOnClickListener {
            prefs.logoutUser()
            startActivity(Intent(requireContext(), SignInActivity::class.java))
            activity?.finish()
        }
    }

    private fun setupDarkModeChips() {
        // Set initial selection
        when (prefs.getDarkMode()) {
            1 -> binding.cgDarkMode.check(R.id.chipThemeLight)
            2 -> binding.cgDarkMode.check(R.id.chipThemeDark)
            else -> binding.cgDarkMode.check(R.id.chipThemeSystem)
        }

        binding.cgDarkMode.setOnCheckedStateChangeListener { _, checkedIds ->
            val mode = when {
                checkedIds.contains(R.id.chipThemeLight) -> 1
                checkedIds.contains(R.id.chipThemeDark)  -> 2
                else -> 0
            }
            prefs.setDarkMode(mode)
            val nightMode = when (mode) {
                1 -> AppCompatDelegate.MODE_NIGHT_NO
                2 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }

    private fun setupReminderSection() {
        val hour = prefs.getReminderHour()
        val minute = prefs.getReminderMinute()
        binding.tvReminderTime.text = String.format("%02d:%02d", hour, minute)
        binding.switchReminder.isChecked = prefs.getReminderEnabled()

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            prefs.setReminderEnabled(isChecked)
            if (isChecked) {
                ReminderScheduler.schedule(requireContext(), prefs.getReminderHour(), prefs.getReminderMinute())
                Toast.makeText(context, "Reminder set for ${binding.tvReminderTime.text}", Toast.LENGTH_SHORT).show()
            } else {
                ReminderScheduler.cancel(requireContext())
                Toast.makeText(context, "Reminder cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSetReminderTime.setOnClickListener {
            val h = prefs.getReminderHour()
            val m = prefs.getReminderMinute()
            TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                prefs.setReminderTime(selectedHour, selectedMinute)
                binding.tvReminderTime.text = String.format("%02d:%02d", selectedHour, selectedMinute)
                if (prefs.getReminderEnabled()) {
                    ReminderScheduler.schedule(requireContext(), selectedHour, selectedMinute)
                }
            }, h, m, true).show()
        }
    }

    private fun setupTimerDurationSection() {
        // Load current custom durations (or defaults)
        pomodoroMin   = prefs.getCustomDuration(SessionType.POMODORO)   ?: SessionType.POMODORO.minutes
        deepFocusMin  = prefs.getCustomDuration(SessionType.DEEP_FOCUS)  ?: SessionType.DEEP_FOCUS.minutes
        ultraFocusMin = prefs.getCustomDuration(SessionType.ULTRA_FOCUS) ?: SessionType.ULTRA_FOCUS.minutes

        binding.tvPomodoroMin.text   = "${pomodoroMin}m"
        binding.tvDeepFocusMin.text  = "${deepFocusMin}m"
        binding.tvUltraFocusMin.text = "${ultraFocusMin}m"

        // Pomodoro
        binding.btnPomodoroMinus.setOnClickListener {
            if (pomodoroMin > 5) { pomodoroMin -= 5; saveDuration(SessionType.POMODORO, pomodoroMin) }
            binding.tvPomodoroMin.text = "${pomodoroMin}m"
        }
        binding.btnPomodoroPlus.setOnClickListener {
            if (pomodoroMin < 120) { pomodoroMin += 5; saveDuration(SessionType.POMODORO, pomodoroMin) }
            binding.tvPomodoroMin.text = "${pomodoroMin}m"
        }

        // Deep Focus
        binding.btnDeepFocusMinus.setOnClickListener {
            if (deepFocusMin > 5) { deepFocusMin -= 5; saveDuration(SessionType.DEEP_FOCUS, deepFocusMin) }
            binding.tvDeepFocusMin.text = "${deepFocusMin}m"
        }
        binding.btnDeepFocusPlus.setOnClickListener {
            if (deepFocusMin < 180) { deepFocusMin += 5; saveDuration(SessionType.DEEP_FOCUS, deepFocusMin) }
            binding.tvDeepFocusMin.text = "${deepFocusMin}m"
        }

        // Ultra Focus
        binding.btnUltraFocusMinus.setOnClickListener {
            if (ultraFocusMin > 5) { ultraFocusMin -= 5; saveDuration(SessionType.ULTRA_FOCUS, ultraFocusMin) }
            binding.tvUltraFocusMin.text = "${ultraFocusMin}m"
        }
        binding.btnUltraFocusPlus.setOnClickListener {
            if (ultraFocusMin < 240) { ultraFocusMin += 5; saveDuration(SessionType.ULTRA_FOCUS, ultraFocusMin) }
            binding.tvUltraFocusMin.text = "${ultraFocusMin}m"
        }
    }

    private fun saveDuration(type: SessionType, minutes: Int) {
        prefs.setCustomDuration(type, minutes)
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
        statsVm.loadStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
