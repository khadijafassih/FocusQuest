package com.example.focusquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.focusquest.databinding.FragmentLeaderboardBinding
import com.example.focusquest.databinding.ItemLeaderboardBinding

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        val prefs = UserPreferencesManager(requireContext())
        val currentUser = prefs.getCurrentUser()
        val allUsers = prefs.getAllUsers().sortedByDescending { it.xp }

        val currentRank = allUsers.indexOfFirst { it.username == currentUser?.username }

        currentUser?.let {
            binding.tvYourName.text = it.username
            binding.tvYourLevel.text = "Level ${it.level}"
            binding.tvYourXP.text = "${it.xp} XP"
            binding.tvYourRank.text = if (currentRank >= 0) "#${currentRank + 1}" else "#-"
        }

        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderboard.adapter = LeaderboardAdapter(allUsers, currentUser?.username)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private class LeaderboardAdapter(
    private val users: List<User>,
    private val currentUsername: String?
) : RecyclerView.Adapter<LeaderboardAdapter.VH>() {

    inner class VH(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val user = users[position]
        val b = holder.binding
        b.tvRank.text = rankLabel(position)
        b.tvUsername.text = user.username
        b.tvLevel.text = "Level ${user.level}"
        b.tvXP.text = "${user.xp} XP"

        if (user.username == currentUsername) {
            b.root.strokeColor = holder.itemView.context.getColor(R.color.primary)
            b.root.strokeWidth = 2
        } else {
            b.root.strokeColor = holder.itemView.context.getColor(R.color.card_stroke)
            b.root.strokeWidth = 1
        }
    }

    private fun rankLabel(position: Int) = when (position) {
        0 -> "🥇"
        1 -> "🥈"
        2 -> "🥉"
        else -> "#${position + 1}"
    }
}
