package com.example.focusquest

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.focusquest.data.db.entity.TaskEntity
import com.example.focusquest.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onComplete: (TaskEntity) -> Unit,
    private val onEdit: (TaskEntity) -> Unit,
    private val onDelete: (TaskEntity) -> Unit
) : ListAdapter<TaskEntity, TaskAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TaskEntity>() {
            override fun areItemsTheSame(old: TaskEntity, new: TaskEntity) = old.id == new.id
            override fun areContentsTheSame(old: TaskEntity, new: TaskEntity) = old == new
        }
        private val DATE_FMT = SimpleDateFormat("MMM d", Locale.getDefault())
    }

    inner class ViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(position)
        val ctx = holder.itemView.context

        holder.binding.tvTaskTitle.text = task.title
        holder.binding.tvCategory.text = task.category

        // Priority stripe color
        val priorityColor = when (task.priority) {
            TaskEntity.PRIORITY_HIGH -> ContextCompat.getColor(ctx, R.color.priority_high)
            TaskEntity.PRIORITY_LOW -> ContextCompat.getColor(ctx, R.color.priority_low)
            else -> ContextCompat.getColor(ctx, R.color.priority_medium)
        }
        holder.binding.viewPriorityStripe.setBackgroundColor(priorityColor)

        // Due date
        if (task.dueDate != null) {
            holder.binding.tvDueDate.visibility = View.VISIBLE
            holder.binding.tvDueDate.text = "Due ${DATE_FMT.format(Date(task.dueDate))}"
            val isOverdue = task.dueDate < System.currentTimeMillis() && !task.isCompleted
            holder.binding.tvDueDate.setTextColor(
                ContextCompat.getColor(ctx, if (isOverdue) R.color.error else R.color.text_secondary)
            )
        } else {
            holder.binding.tvDueDate.visibility = View.GONE
        }

        // XP chip
        holder.binding.tvXP.text = "+${task.xpReward} XP"

        // Completed state
        holder.binding.cbCompleted.setOnCheckedChangeListener(null)
        holder.binding.cbCompleted.isChecked = task.isCompleted
        if (task.isCompleted) {
            holder.binding.tvTaskTitle.paintFlags = holder.binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.binding.tvTaskTitle.alpha = 0.5f
        } else {
            holder.binding.tvTaskTitle.paintFlags = holder.binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.binding.tvTaskTitle.alpha = 1f
        }

        holder.binding.cbCompleted.setOnCheckedChangeListener { _, _ -> onComplete(task) }
        holder.binding.root.setOnClickListener { onEdit(task) }
        holder.binding.root.setOnLongClickListener { onDelete(task); true }
    }

    fun getTaskAt(position: Int): TaskEntity = currentList[position]
}
