package com.example.focusquest

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.focusquest.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.ivProfile.setImageURI(it)
            saveProfileImage(it.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadProfile()

        binding.btnSelectImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnResetStats.setOnClickListener {
            resetStats()
        }
    }

    private fun loadProfile() {
        val prefs = requireContext().getSharedPreferences("FocusQuestPrefs", Context.MODE_PRIVATE)
        val xp = prefs.getInt("XP", 0)
        val level = (xp / 100) + 1
        val imageUri = prefs.getString("ProfileImage", null)

        binding.tvProfileStats.text = "Level $level | $xp Total XP"
        
        imageUri?.let {
            binding.ivProfile.setImageURI(Uri.parse(it))
        }
    }

    private fun saveProfileImage(uriString: String) {
        val prefs = requireContext().getSharedPreferences("FocusQuestPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("ProfileImage", uriString).apply()
    }

    private fun resetStats() {
        val prefs = requireContext().getSharedPreferences("FocusQuestPrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("XP", 0).apply()
        loadProfile()
        Toast.makeText(context, "Progression Reset", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
