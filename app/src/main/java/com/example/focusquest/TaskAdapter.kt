package com.example.focusquest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.focusquest.databinding.ItemTaskBinding

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskChecked: (Task, Boolean) -> Unit,
    private val onTaskDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.binding.tvTaskTitle.text = task.title
        holder.binding.tvCategory.text = task.category
        holder.binding.tvXP.text = "+${task.xpReward} XP"

        holder.binding.cbCompleted.setOnCheckedChangeListener(null)
        holder.binding.cbCompleted.isChecked = task.isCompleted

        holder.binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            onTaskChecked(task, isChecked)
        }

        holder.binding.tvTaskTitle.alpha = if (task.isCompleted) 0.5f else 1.0f

        holder.binding.root.setOnLongClickListener {
            onTaskDelete(task)
            true
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}