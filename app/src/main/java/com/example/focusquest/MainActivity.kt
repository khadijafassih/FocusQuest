package com.example.focusquest

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.focusquest.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener(this)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_tasks -> replaceFragment(TaskFragment())
                R.id.nav_timer -> replaceFragment(TimerFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
            }
            true
        }

        // Default fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                replaceFragment(HomeFragment())
                binding.bottomNavigation.selectedItemId = R.id.nav_home
            }
            R.id.nav_tasks -> {
                replaceFragment(TaskFragment())
                binding.bottomNavigation.selectedItemId = R.id.nav_tasks
            }
            R.id.nav_timer -> {
                replaceFragment(TimerFragment())
                binding.bottomNavigation.selectedItemId = R.id.nav_timer
            }
            R.id.nav_profile -> {
                replaceFragment(ProfileFragment())
                binding.bottomNavigation.selectedItemId = R.id.nav_profile
            }
            R.id.nav_reset -> {
                resetData()
            }
            R.id.nav_about -> {
                Toast.makeText(this, "FocusQuest v1.0", Toast.LENGTH_SHORT).show()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun resetData() {
        val prefs = getSharedPreferences("FocusQuestPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
        Toast.makeText(this, "Data Reset", Toast.LENGTH_SHORT).show()
        replaceFragment(HomeFragment())
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
