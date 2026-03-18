package com.example.historialclinico.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.historialclinico.R
import com.example.historialclinico.ui.fragments.ExpedienteFragment
import com.example.historialclinico.ui.fragments.HomeFragment
import com.example.historialclinico.ui.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Si no hay sesión, regresar al login
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        bottomNav = findViewById(R.id.bottomNav)

        if (savedInstanceState == null) loadFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home       -> { loadFragment(HomeFragment());       true }
                R.id.nav_expediente -> { loadFragment(ExpedienteFragment()); true }
                R.id.nav_profile    -> { loadFragment(ProfileFragment());    true }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
