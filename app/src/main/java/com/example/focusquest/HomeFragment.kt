package com.example.focusquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.focusquest.databinding.FragmentHomeBinding
import com.example.focusquest.viewmodel.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val vm: HomeViewModel by viewModels()
    private lateinit var prefs: UserPreferencesManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = UserPreferencesManager(requireContext())

        setupHeader()
        observeViewModel()
        setupButtons()
    }

    private fun setupHeader() {
        val user = prefs.getCurrentUser()
        val name = user?.username?.replaceFirstChar { it.uppercase() } ?: "Hero"
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
        binding.tvGreeting.text = "$greeting, $name"
        binding.tvDate.text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }

    private fun observeViewModel() {
        vm.quote.observe(viewLifecycleOwner) { q ->
            binding.tvQuote.text = q?.quote ?: "Stay focused and make it happen."
            binding.tvQuoteAuthor.text = "— ${q?.author ?: "Unknown"}"
        }

        vm.isLoadingQuote.observe(viewLifecycleOwner) { loading ->
            binding.progressQuote.visibility = if (loading) View.VISIBLE else View.GONE
        }

        vm.weather.observe(viewLifecycleOwner) { w ->
            if (w != null) {
                binding.tvWeatherCity.text = w.city
                binding.tvWeatherTemp.text = "${w.temperature}°C"
                binding.tvWeatherCondition.text = w.condition
                binding.tvWeatherEmoji.text = w.emoji
                binding.cardWeather.visibility = View.VISIBLE
            }
        }

        vm.bgImageUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrBlank()) {
                Glide.with(this)
                    .load(url)
                    .centerCrop()
                    .into(binding.ivQuoteBg)
            }
        }

        vm.tasksDoneToday.observe(viewLifecycleOwner) { binding.tvTasksDone.text = it.toString() }
        vm.sessionsToday.observe(viewLifecycleOwner) { binding.tvSessionsCount.text = it.toString() }
        vm.focusMinutesToday.observe(viewLifecycleOwner) { min ->
            binding.tvFocusTime.text = if (min >= 60) "${min / 60}h ${min % 60}m" else "${min}m"
        }
    }

    private fun setupButtons() {
        binding.btnRefreshQuote.setOnClickListener { vm.loadQuote() }
        binding.btnStartFocus.setOnClickListener { navigateTo(R.id.nav_timer) }
        binding.btnAddTask.setOnClickListener { navigateTo(R.id.nav_tasks) }
    }

    private fun navigateTo(itemId: Int) {
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = itemId
    }

    override fun onResume() {
        super.onResume()
        vm.loadAll()
        setupHeader()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
