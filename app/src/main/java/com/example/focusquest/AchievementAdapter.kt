package com.example.focusquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AchievementAdapter(
    private val allAchievements: List<AchievementManager.AchievementDef>,
    private val unlockedIds: Set<String>
) : RecyclerView.Adapter<AchievementAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView   = view.findViewById(R.id.tvAchievementEmoji)
        val tvTitle: TextView   = view.findViewById(R.id.tvAchievementTitle)
        val tvDesc: TextView    = view.findViewById(R.id.tvAchievementDesc)
        val tvStatus: TextView  = view.findViewById(R.id.tvAchievementStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return VH(view)
    }

    override fun getItemCount() = allAchievements.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ach = allAchievements[position]
        val unlocked = ach.id in unlockedIds

        holder.tvEmoji.text = ach.emoji
        holder.tvTitle.text = ach.title
        holder.tvDesc.text = ach.desc

        if (unlocked) {
            holder.tvStatus.visibility = View.VISIBLE
            holder.itemView.alpha = 1f
        } else {
            holder.tvStatus.visibility = View.GONE
            holder.itemView.alpha = 0.4f
        }
    }
}
