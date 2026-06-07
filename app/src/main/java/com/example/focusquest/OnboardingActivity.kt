package com.example.focusquest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.focusquest.databinding.ActivityOnboardingBinding
import com.google.android.material.chip.ChipGroup

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var prefs: UserPreferencesManager
    private var selectedGoal = 4

    data class OnboardingPage(
        val emoji: String,
        val title: String,
        val subtitle: String,
        val showGoalPicker: Boolean = false
    )

    private val pages = listOf(
        OnboardingPage(
            emoji = "🍅",
            title = "Welcome to FocusQuest",
            subtitle = "The Pomodoro technique splits your work into focused intervals with short breaks."
        ),
        OnboardingPage(
            emoji = "🏆",
            title = "Earn XP & Level Up",
            subtitle = "Complete focus sessions and tasks to earn XP, unlock achievements, and level up."
        ),
        OnboardingPage(
            emoji = "🎯",
            title = "Set Your Daily Goal",
            subtitle = "How many focus sessions do you want to complete per day?",
            showGoalPicker = true
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = UserPreferencesManager(this)

        val adapter = OnboardingAdapter()
        binding.viewPager.adapter = adapter

        setupDots()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                if (position == pages.lastIndex) {
                    binding.btnNext.text = "Get Started"
                    binding.btnSkip.visibility = View.GONE
                } else {
                    binding.btnNext.text = "Next"
                    binding.btnSkip.visibility = View.VISIBLE
                }
            }
        })

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pages.lastIndex) {
                binding.viewPager.currentItem = current + 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener { finishOnboarding() }
    }

    private fun setupDots() {
        binding.dotsContainer.removeAllViews()
        pages.forEachIndexed { index, _ ->
            val dot = ImageView(this).apply {
                val params = LinearLayout.LayoutParams(12, 12).apply {
                    setMargins(6, 0, 6, 0)
                }
                layoutParams = params
                setImageResource(if (index == 0) R.drawable.dot_active else R.drawable.dot_inactive)
            }
            binding.dotsContainer.addView(dot)
        }
    }

    private fun updateDots(position: Int) {
        for (i in 0 until binding.dotsContainer.childCount) {
            val dot = binding.dotsContainer.getChildAt(i) as? ImageView ?: continue
            dot.setImageResource(if (i == position) R.drawable.dot_active else R.drawable.dot_inactive)
        }
    }

    private fun finishOnboarding() {
        prefs.setDailyGoal(selectedGoal)
        prefs.setHasSeenOnboarding(true)
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    inner class OnboardingAdapter : RecyclerView.Adapter<OnboardingAdapter.PageVH>() {

        inner class PageVH(view: View) : RecyclerView.ViewHolder(view) {
            val tvEmoji: TextView = view.findViewById(R.id.tvEmoji)
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
            val cgGoal: ChipGroup = view.findViewById(R.id.cgGoal)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageVH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_onboarding_page, parent, false)
            return PageVH(view)
        }

        override fun getItemCount() = pages.size

        override fun onBindViewHolder(holder: PageVH, position: Int) {
            val page = pages[position]
            holder.tvEmoji.text = page.emoji
            holder.tvTitle.text = page.title
            holder.tvSubtitle.text = page.subtitle

            if (page.showGoalPicker) {
                holder.cgGoal.visibility = View.VISIBLE
                // Set initial selection based on prefs
                val goal = prefs.getDailyGoal()
                when (goal) {
                    2 -> holder.cgGoal.check(R.id.chip2)
                    6 -> holder.cgGoal.check(R.id.chip6)
                    else -> holder.cgGoal.check(R.id.chip4)
                }
                holder.cgGoal.setOnCheckedStateChangeListener { _, checkedIds ->
                    selectedGoal = when {
                        checkedIds.contains(R.id.chip2) -> 2
                        checkedIds.contains(R.id.chip6) -> 6
                        else -> 4
                    }
                }
            } else {
                holder.cgGoal.visibility = View.GONE
            }
        }
    }
}
