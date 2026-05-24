package com.example.focusquest

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.focusquest.data.db.entity.TaskEntity
import com.example.focusquest.databinding.DialogAddTaskBinding
import com.example.focusquest.databinding.FragmentTasksBinding
import com.example.focusquest.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val vm: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter
    private var allTasks = listOf<TaskEntity>()
    private var activeFilter = "ALL"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFilters()
        observeTasks()
        observeAchievements()

        binding.fabAddTask.setOnClickListener { showAddTaskDialog(null) }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onComplete = { task ->
                if (!task.isCompleted) {
                    vm.complete(task)
                    showXpToast(task.xpReward)
                } else {
                    vm.uncomplete(task)
                }
            },
            onEdit = { showAddTaskDialog(it) },
            onDelete = { task ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Task")
                    .setMessage("Delete \"${task.title}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        vm.delete(task)
                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(context)
        binding.rvTasks.adapter = adapter

        // Swipe to delete
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                val task = adapter.getTaskAt(vh.adapterPosition)
                vm.delete(task)
                Toast.makeText(context, "Task removed", Toast.LENGTH_SHORT).show()
            }
        }).attachToRecyclerView(binding.rvTasks)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { applyFilter() }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilters() {
        binding.chipAll.setOnClickListener { setFilter("ALL") }
        binding.chipPending.setOnClickListener { setFilter("PENDING") }
        binding.chipCompleted.setOnClickListener { setFilter("COMPLETED") }
        setFilter("ALL")
    }

    private fun setFilter(filter: String) {
        activeFilter = filter
        binding.chipAll.isChecked = filter == "ALL"
        binding.chipPending.isChecked = filter == "PENDING"
        binding.chipCompleted.isChecked = filter == "COMPLETED"
        applyFilter()
    }

    private fun observeTasks() {
        vm.allTasks().observe(viewLifecycleOwner) { tasks ->
            allTasks = tasks
            applyFilter()
        }
    }

    private fun observeAchievements() {
        vm.newAchievements.observe(viewLifecycleOwner) { achievements ->
            if (achievements.isNotEmpty()) {
                val names = achievements.joinToString("\n") { "${it.emoji} ${it.title}" }
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Achievement Unlocked!")
                    .setMessage(names)
                    .setPositiveButton("Awesome!", null)
                    .show()
                vm.acknowledgeAchievements()
            }
        }
    }

    private fun applyFilter() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val filtered = allTasks.filter { task ->
            val matchQuery = query.isEmpty() || task.title.lowercase().contains(query)
            val matchFilter = when (activeFilter) {
                "PENDING" -> !task.isCompleted
                "COMPLETED" -> task.isCompleted
                else -> true
            }
            matchQuery && matchFilter
        }
        adapter.submitList(filtered)
        binding.layoutEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showAddTaskDialog(taskToEdit: TaskEntity?) {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(context))
        var selectedDueDate: Long? = taskToEdit?.dueDate

        taskToEdit?.let { task ->
            dialogBinding.etTaskTitle.setText(task.title)
            dialogBinding.etTaskDesc.setText(task.description)
            when (task.category) {
                "Study" -> dialogBinding.chipStudy.isChecked = true
                "Work" -> dialogBinding.chipWork.isChecked = true
                "Health" -> dialogBinding.chipHealth.isChecked = true
                else -> dialogBinding.chipPersonal.isChecked = true
            }
            when (task.priority) {
                TaskEntity.PRIORITY_HIGH -> dialogBinding.chipHigh.isChecked = true
                TaskEntity.PRIORITY_LOW -> dialogBinding.chipLow.isChecked = true
                else -> dialogBinding.chipMedium.isChecked = true
            }
            task.dueDate?.let { date ->
                dialogBinding.tvDueDateSelected.text =
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(date))
            }
        } ?: run {
            dialogBinding.chipMedium.isChecked = true
            dialogBinding.chipPersonal.isChecked = true
        }

        dialogBinding.btnDueDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d, 23, 59, 59)
                selectedDueDate = cal.timeInMillis
                dialogBinding.tvDueDateSelected.text =
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(cal.time)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (taskToEdit == null) "New Task" else "Edit Task")
            .setView(dialogBinding.root)
            .setPositiveButton(if (taskToEdit == null) "Add" else "Save") { _, _ ->
                val title = dialogBinding.etTaskTitle.text.toString().trim()
                if (title.isEmpty()) { Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show(); return@setPositiveButton }

                val category = when {
                    dialogBinding.chipStudy.isChecked -> "Study"
                    dialogBinding.chipWork.isChecked -> "Work"
                    dialogBinding.chipHealth.isChecked -> "Health"
                    else -> "Personal"
                }
                val priority = when {
                    dialogBinding.chipHigh.isChecked -> TaskEntity.PRIORITY_HIGH
                    dialogBinding.chipLow.isChecked -> TaskEntity.PRIORITY_LOW
                    else -> TaskEntity.PRIORITY_MEDIUM
                }
                val xp = TaskEntity.xpForCategory(category, priority)

                if (taskToEdit == null) {
                    vm.add(TaskEntity(
                        username = vm.username,
                        title = title,
                        description = dialogBinding.etTaskDesc.text.toString().trim(),
                        category = category,
                        priority = priority,
                        dueDate = selectedDueDate,
                        xpReward = xp
                    ))
                    Toast.makeText(context, "+$xp XP when completed!", Toast.LENGTH_SHORT).show()
                } else {
                    vm.update(taskToEdit.copy(
                        title = title,
                        description = dialogBinding.etTaskDesc.text.toString().trim(),
                        category = category,
                        priority = priority,
                        dueDate = selectedDueDate,
                        xpReward = xp
                    ))
                    Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showXpToast(xp: Int) {
        Toast.makeText(context, "Task complete! +$xp XP", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
