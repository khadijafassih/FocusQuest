package com.example.focusquest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.focusquest.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
    }

    private fun updateUI() {
        val prefs = requireContext().getSharedPreferences("FocusQuestPrefs", Context.MODE_PRIVATE)
        val xp = prefs.getInt("XP", 0)
        val level = (xp / 100) + 1
        val progress = xp % 100

        binding.tvLevel.text = "Level: $level"
        binding.pbXP.progress = progress
        binding.tvXPLabel.text = "$progress / 100 XP"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
