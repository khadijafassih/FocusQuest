package com.example.focusquest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.focusquest.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var quotePageCallback: ViewPager2.OnPageChangeCallback? = null
    private lateinit var userPrefs: UserPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPrefs = UserPreferencesManager(requireContext())
        updateUI()
        setupQuickActions()
        setupQuoteSlider()
    }

    private fun setupQuickActions() {
        binding.cardAddTask.setOnClickListener {
            navigateToTab(R.id.nav_tasks)
        }

        binding.cardStartSession.setOnClickListener {
            navigateToTab(R.id.nav_timer)
        }
    }

    private fun navigateToTab(tabId: Int) {
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav?.selectedItemId = tabId
    }

    private fun updateUI() {
        val currentUser = userPrefs.getCurrentUser()
        if (currentUser != null) {
            val level = currentUser.level
            val progress = currentUser.xp % 100

            binding.tvLevel.text = "LEVEL\n$level"
            binding.pbXP.progress = progress
            binding.tvXPLabel.text = "$progress / 100\nXP"
        }
    }

    private fun setupQuoteSlider() {
        val slides = listOf(
            QuoteSlide("The best time to focus is now.", R.drawable.quote_nature_1),
            QuoteSlide("Small progress every day adds up.", R.drawable.quote_nature_2),
            QuoteSlide("Discipline turns goals into reality.", R.drawable.quote_nature_3),
            QuoteSlide("One session at a time, one level higher.", R.drawable.quote_nature_4),
            QuoteSlide("Breathe, focus, and move forward.", R.drawable.quote_nature_5)
        )

        binding.viewPagerQuotes.adapter = QuoteSliderAdapter(slides)
        binding.tvQuotePage.text = "1/${slides.size}"

        quotePageCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tvQuotePage.text = "${position + 1}/${slides.size}"
            }
        }
        binding.viewPagerQuotes.registerOnPageChangeCallback(quotePageCallback!!)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onDestroyView() {
        quotePageCallback?.let { binding.viewPagerQuotes.unregisterOnPageChangeCallback(it) }
        quotePageCallback = null
        super.onDestroyView()
        _binding = null
    }
}
