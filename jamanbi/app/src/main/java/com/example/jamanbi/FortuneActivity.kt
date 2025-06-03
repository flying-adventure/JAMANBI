package com.example.jamanbi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class FortuneActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var resultText: TextView
    private lateinit var predictButton: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val fortuneMessages = listOf(
        "당신은..합격할 것..",
        "요행을 바라지 마세요..",
        "더 공부하세요.."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fortune)

        spinner = findViewById(R.id.spinnerSavedCerts)
        predictButton = findViewById(R.id.btnPredict)
        resultText = findViewById(R.id.tvResult)

        setupBottomNav()
        loadSavedCertsToSpinner()

        predictButton.setOnClickListener {
            val message = fortuneMessages.random()
            resultText.visibility = View.VISIBLE
            resultText.text = message
        }
    }

    private fun loadSavedCertsToSpinner() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("savedCerts")
            .get()
            .addOnSuccessListener { result ->
                val certNames = result.mapNotNull { it.getString("name") }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, certNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "자격증 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_fortune
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_cert -> {
                    startActivity(Intent(this, SearchCertActivity::class.java))
                    true
                }
                R.id.nav_fortune -> true
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
