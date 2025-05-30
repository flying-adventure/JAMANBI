package com.example.jamanbi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // 로그인된 상태 → 게시판으로 이동
            startActivity(Intent(this, PostListActivity::class.java))
            finish()
        } else {
            // 로그인 안된 상태 → activity_login.xml 표시
            setContentView(R.layout.activity_login)
        }
    }
}
