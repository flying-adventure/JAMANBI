package com.example.jamanbi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class SearchCertActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchcert)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_cert

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_cert -> true //현재 페이지
                R.id.nav_schedule -> { startActivity(Intent(this, PostListActivity::class.java))
                    true}
                R.id.nav_board -> {
                    startActivity(Intent(this, PostListActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
