package com.example.focusquest

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.focusquest.databinding.FragmentTasksBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskFragment : Fragment() {
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter
    private var allTasks = mutableListOf<Task>()
    private var filteredTasks = mutableListOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadTasks()
        setupFilter()
        setupRecyclerView()
        setupSearch()

        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            filteredTasks,
            onTaskChecked = { task, isChecked ->
                task.isCompleted = isChecked
                if (isChecked) {
                    addXP(task.xpReward)
                    Toast.makeText(context, "+${task.xpReward} XP Earned! 🎉", Toast.LENGTH_SHORT).show()
                }
                saveTasks()
                applyFilters()
            },
            onTaskDelete = { task ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Task")
                    .setMessage("Delete \"${task.title}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        allTasks.remove(task)
                        saveTasks()
                        applyFilters()
                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(context)
        binding.rvTasks.adapter = taskAdapter
        applyFilters()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilter() {
        val options = arrayOf("All", "Pending", "Completed")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter

        binding.chipAll.setOnClickListener {
            binding.spinnerFilter.setSelection(0)
        }

        binding.chipPending.setOnClickListener {
            binding.spinnerFilter.setSelection(1)
        }

        binding.chipCompleted.setOnClickListener {
            binding.spinnerFilter.setSelection(2)
        }

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateFilterChips(position)
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        updateFilterChips(0)
    }

    private fun updateFilterChips(position: Int) {
        binding.chipAll.isSelected = position == 0
        binding.chipPending.isSelected = position == 1
        binding.chipCompleted.isSelected = position == 2
    }

    private fun applyFilters() {
        val query = binding.etSearch.text.toString().lowercase()
        val filterStatus = binding.spinnerFilter.selectedItem?.toString() ?: "All"

        val list = allTasks.filter { task ->
            val matchesQuery = task.title.lowercase().contains(query)
            val matchesFilter = when (filterStatus) {
                "Pending" -> !task.isCompleted
                "Completed" -> task.isCompleted
                else -> true
            }
            matchesQuery && matchesFilter
        }
        filteredTasks.clear()
        filteredTasks.addAll(list)
        taskAdapter.updateTasks(filteredTasks.toList())
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_task, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        val categories = arrayOf("Study", "Work", "Personal")
        spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isNotEmpty()) {
                    val category = spinnerCategory.selectedItem.toString()
                    val xp = when (category) {
                        "Study" -> 20
                        "Work" -> 15
                        else -> 10
                    }
                    val newTask = Task(title = title, category = category, xpReward = xp)
                    allTasks.add(newTask)
                    saveTasks()
                    applyFilters()
                    Toast.makeText(context, "Task Added ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addXP(amount: Int) {
        val prefs = requireContext().getSharedPreferences("FocusQuestPrefs", Context.MODE_PRIVATE)
        val currentXP = prefs.getInt("XP", 0)
        prefs.edit().putInt("XP", currentXP + amount).apply()
    }

    private fun saveTasks() {
        val prefs = requireContext().getSharedPreferences("FocusQuestPrefs", Context.MODE_PRIVATE)
        val json = Gson().toJson(allTasks)
        prefs.edit().putString("Tasks", json).apply()
    }

    private fun loadTasks() {
        val prefs = requireContext().getSharedPreferences("FocusQuestPrefs", Context.MODE_PRIVATE)
        val json = prefs.getString("Tasks", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Task>>() {}.type
            allTasks = Gson().fromJson(json, type)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}