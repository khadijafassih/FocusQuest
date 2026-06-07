package com.example.focusquest

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.focusquest.databinding.FragmentStatsBinding
import com.example.focusquest.viewmodel.StatsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val vm: StatsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        setupChart()
        observeViewModel()
    }

    private fun setupChart() {
        binding.chartWeekly.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            isHighlightPerTapEnabled = false
            setScaleEnabled(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(
                    arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                )
                textColor = Color.parseColor("#6B7280")
                textSize = 11f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#F3F4F6")
                axisMinimum = 0f
                textColor = Color.parseColor("#6B7280")
                textSize = 10f
                granularity = 0.5f
            }
            axisRight.isEnabled = false
        }
    }

    private fun observeViewModel() {
        vm.weeklyHours.observe(viewLifecycleOwner) { hours ->
            if (hours.isEmpty()) return@observe
            val entries = hours.mapIndexed { i, h -> BarEntry(i.toFloat(), h) }
            val dataSet = BarDataSet(entries, "Focus Hours").apply {
                color = Color.parseColor("#2563EB")
                setDrawValues(true)
                valueTextColor = Color.parseColor("#374151")
                valueTextSize = 10f
            }
            binding.chartWeekly.data = BarData(dataSet).apply { barWidth = 0.5f }
            binding.chartWeekly.invalidate()
            binding.chartWeekly.animateY(600)
        }

        vm.totalSessions.observe(viewLifecycleOwner) { binding.tvTotalSessions.text = it.toString() }
        vm.totalHours.observe(viewLifecycleOwner) { binding.tvTotalHours.text = String.format("%.1f", it) }
        vm.totalTasksCompleted.observe(viewLifecycleOwner) { binding.tvTotalTasks.text = it.toString() }
        vm.currentStreak.observe(viewLifecycleOwner) {
            binding.tvCurrentStreak.text = "$it"
            binding.tvStreakLabel.text = if (it == 1) "day streak" else "day streak"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
