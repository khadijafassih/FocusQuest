package com.example.focusquest

import android.content.Intent
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
    private lateinit var userPrefs: UserPreferencesManager

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.ivProfile.setImageURI(it)
            userPrefs.updateProfileImage(it.toString())
            Toast.makeText(context, "Profile photo updated", Toast.LENGTH_SHORT).show()
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
        userPrefs = UserPreferencesManager(requireContext())
        
        loadProfile()

        binding.btnSelectImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnResetStats.setOnClickListener {
            resetStats()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadProfile() {
        val currentUser = userPrefs.getCurrentUser()
        if (currentUser != null) {
            binding.tvProfileName.text = currentUser.username
            binding.tvProfileStats.text = "Level ${currentUser.level} | ${currentUser.xp} Total XP"
            
            currentUser.profileImage?.let {
                binding.ivProfile.setImageURI(Uri.parse(it))
            }
        }
    }

    private fun resetStats() {
        val currentUser = userPrefs.getCurrentUser()
        if (currentUser != null) {
            userPrefs.setCurrentUserXP(0)
            loadProfile()
            Toast.makeText(context, "Progression Reset", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        userPrefs.logoutUser()
        startActivity(Intent(requireContext(), SignInActivity::class.java))
        activity?.finish()
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
