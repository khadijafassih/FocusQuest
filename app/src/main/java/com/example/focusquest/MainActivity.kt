package com.example.focusquest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    private lateinit var userPrefs: UserPreferencesManager

    private val systemReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_LOW -> {
                    Toast.makeText(context, "Battery Low! Please charge your phone!", Toast.LENGTH_LONG).show()
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    Toast.makeText(context, "Power Connected! Charging... ⚡", Toast.LENGTH_SHORT).show()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    Toast.makeText(context, "Power Disconnected", Toast.LENGTH_SHORT).show()
                }
                Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                    val isAirplaneModeOn = intent.getBooleanExtra("state", false)
                    val message = if (isAirplaneModeOn) "Airplane Mode ON ✈️" else "Airplane Mode OFF"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPrefs = UserPreferencesManager(this)

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

        // Register system broadcast receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        registerReceiver(systemReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(systemReceiver)
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
            R.id.nav_logout -> {
                logoutUser()
            }
            R.id.nav_about -> {
                Toast.makeText(this, "FocusQuest v1.0", Toast.LENGTH_SHORT).show()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logoutUser() {
        userPrefs.logoutUser()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
