package com.example.focusquest

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.focusquest.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userPrefs: UserPreferencesManager
    private val auth = FirebaseAuth.getInstance()

    private val systemReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val ctx = context ?: return
            when (intent?.action) {
                Intent.ACTION_BATTERY_LOW         -> NotificationHelper.postBatteryLow(ctx)
                Intent.ACTION_POWER_CONNECTED     -> NotificationHelper.postCharging(ctx)
                Intent.ACTION_POWER_DISCONNECTED  -> NotificationHelper.postUnplugged(ctx)
                Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                    val on = intent.getBooleanExtra("state", false)
                    NotificationHelper.postAirplaneMode(ctx, on)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply dark mode preference
        val tempPrefs = UserPreferencesManager(this)
        val nightMode = when (tempPrefs.getDarkMode()) {
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPrefs = UserPreferencesManager(this)
        setSupportActionBar(binding.toolbar)

        requestNotificationPermissionIfNeeded()

        // Ensure user is signed in to Firebase anonymously for Firestore access
        if (auth.currentUser == null) {
            auth.signInAnonymously()
        }

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener(this)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> { loadFragment(HomeFragment()); true }
                R.id.nav_tasks   -> { loadFragment(TaskFragment()); true }
                R.id.nav_ai      -> { loadFragment(AiPlannerFragment()); true }
                R.id.nav_timer   -> { loadFragment(TimerFragment()); true }
                R.id.nav_profile -> { loadFragment(ProfileFragment()); true }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        registerReceiver(systemReceiver, filter)
    }

    private fun loadFragment(fragment: Fragment) {
        // Clear back stack when switching tabs
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home    -> { binding.bottomNavigation.selectedItemId = R.id.nav_home }
            R.id.nav_tasks   -> { binding.bottomNavigation.selectedItemId = R.id.nav_tasks }
            R.id.nav_ai      -> { binding.bottomNavigation.selectedItemId = R.id.nav_ai }
            R.id.nav_leaderboard -> { loadFragment(LeaderboardFragment()) }
            R.id.nav_timer   -> { binding.bottomNavigation.selectedItemId = R.id.nav_timer }
            R.id.nav_profile -> { binding.bottomNavigation.selectedItemId = R.id.nav_profile }
            R.id.nav_logout -> { logoutUser() }
            R.id.nav_about -> { Toast.makeText(this, "FocusQuest v1.0 — Stay focused, level up!", Toast.LENGTH_SHORT).show() }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logoutUser() {
        userPrefs.logoutUser()
        auth.signOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(systemReceiver)
    }
}
