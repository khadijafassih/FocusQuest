package com.example.focusquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.focusquest.databinding.FragmentAiPlannerBinding
import com.example.focusquest.viewmodel.AiPlannerViewModel
import com.example.focusquest.viewmodel.AiTask

class AiPlannerFragment : Fragment() {

    private var _binding: FragmentAiPlannerBinding? = null
    private val binding get() = _binding!!
    private val vm: AiPlannerViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAiPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupInput()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.rvChat.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
        }
    }

    private fun setupInput() {
        binding.btnSend.setOnClickListener { sendMessage() }

        binding.etInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        binding.btnCreateTasks.setOnClickListener {
            vm.createTasks()
        }

        binding.btnDismissPlan.setOnClickListener {
            vm.clearPlan()
            binding.cardPlan.visibility = View.GONE
        }

        binding.btnResetSession.setOnClickListener {
            vm.resetSession()
            Toast.makeText(context, "Session reset", Toast.LENGTH_SHORT).show()
        }

        binding.etProjectName.addTextChangedListener {
            vm.projectName.value = it?.toString() ?: ""
        }
    }

    private fun sendMessage() {
        val text = binding.etInput.text?.toString()?.trim() ?: return
        if (text.isEmpty()) return
        binding.etInput.setText("")
        vm.sendMessage(text)
    }

    private fun observeViewModel() {
        vm.messages.observe(viewLifecycleOwner) { msgs ->
            chatAdapter.setMessages(msgs)
            if (msgs.isNotEmpty()) binding.rvChat.scrollToPosition(msgs.size - 1)
        }

        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.layoutTyping.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSend.isEnabled = !loading
        }

        vm.projectName.observe(viewLifecycleOwner) { name ->
            if (binding.etProjectName.text.toString() != name) {
                binding.etProjectName.setText(name)
            }
        }

        vm.parsedTasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isNotEmpty()) {
                binding.cardPlan.visibility = View.VISIBLE
                binding.tvPlanSummary.text = buildPlanSummary(tasks)
            } else {
                binding.cardPlan.visibility = View.GONE
            }
        }

        vm.tasksCreated.observe(viewLifecycleOwner) { done ->
            if (done) {
                binding.cardPlan.visibility = View.GONE
                Toast.makeText(context, "Tasks created! Check the Tasks tab.", Toast.LENGTH_LONG).show()
                vm.acknowledgeTasksCreated()
            }
        }
    }

    private fun buildPlanSummary(tasks: List<AiTask>): String = tasks.joinToString("\n") { task ->
        val dot = when (task.priority.uppercase()) {
            "HIGH"   -> "🔴"
            "MEDIUM" -> "🟡"
            else     -> "🟢"
        }
        "$dot ${task.title}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
