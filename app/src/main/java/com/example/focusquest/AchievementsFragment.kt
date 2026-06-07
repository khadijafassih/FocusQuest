package com.example.focusquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AchievementsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_achievements, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvAchievements = view.findViewById<RecyclerView>(R.id.rvAchievements)
        val tvUnlockedCount = view.findViewById<TextView>(R.id.tvUnlockedCount)
        val btnBack = view.findViewById<MaterialButton>(R.id.btnBack)

        rvAchievements.layoutManager = GridLayoutManager(requireContext(), 2)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val prefs = UserPreferencesManager(requireContext())
            val username = prefs.getCurrentUser()?.username ?: return@launch
            val db = com.example.focusquest.data.db.AppDatabase.getInstance(requireContext())

            val unlockedIds = withContext(Dispatchers.IO) {
                db.achievementDao().getUnlockedIds(username).toSet()
            }

            tvUnlockedCount.text = "${unlockedIds.size} / ${AchievementManager.ALL.size}"
            rvAchievements.adapter = AchievementAdapter(AchievementManager.ALL, unlockedIds)
        }
    }
}
