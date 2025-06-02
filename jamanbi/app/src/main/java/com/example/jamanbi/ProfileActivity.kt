package com.example.jamanbi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val REQ_EDIT_PROFILE = 100
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvUserName: TextView
    private lateinit var tvEmail:    TextView
    private lateinit var tvBirth:    TextView
    private lateinit var tvGender:   TextView
    private lateinit var tvInterest: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        // 뷰 바인딩
        tvUserName  = findViewById(R.id.tvUserName)
        tvEmail     = findViewById(R.id.tvEmail)
        tvBirth     = findViewById(R.id.tvBirth)
        tvGender    = findViewById(R.id.tvGender)
        tvInterest  = findViewById(R.id.tvInterest)

        // Firestore에서 프로필 불러오기
        loadProfileFromFirestore()

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // 로그아웃 및 스택 정리
            auth.signOut()
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(this)
            }
            finish()
        }

        findViewById<Button>(R.id.btnEditInfo).setOnClickListener {
            startActivityForResult(
                Intent(this, EditProfileActivity::class.java),
                REQ_EDIT_PROFILE
            )
        }

        findViewById<Button>(R.id.btnSavedCerts).setOnClickListener {
            startActivity(Intent(this, SavedCertListActivity::class.java))
        }

        findViewById<Button>(R.id.btnWithdraw).setOnClickListener {
            val user = auth.currentUser

            if (user != null) {
                // 1. Firestore 문서 삭제
                db.collection("user").document(user.uid)
                    .delete()
                    .addOnSuccessListener {
                        // 2. Auth 계정 삭제
                        user.delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "회원탈퇴 완료", Toast.LENGTH_SHORT).show()
                                Intent(this, LoginActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(this)
                                }
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "계정 삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "유저 데이터 삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "로그인 정보가 없습니다", Toast.LENGTH_SHORT).show()
            }
        }


        // 하단 네비게이션
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_cert     -> startActivity(Intent(this, SearchCertActivity::class.java))
                R.id.nav_schedule -> startActivity(Intent(this, ScheduleActivity::class.java))
                R.id.nav_board    -> startActivity(Intent(this, PostListActivity::class.java))
                R.id.nav_profile  -> { /* 현재 화면 */ }
            }
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_EDIT_PROFILE && resultCode == RESULT_OK) {
            // 수정 완료 후 다시 Firestore에서 불러오기
            loadProfileFromFirestore()
        }
    }

    private fun loadProfileFromFirestore() {
        val user = auth.currentUser
        if (user == null) {
            // 로그인 정보 없으면 로그인 화면으로
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(this)
            }
            finish()
            return
        }

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    tvUserName.text = doc.getString("name")     ?: "이름 없음"
                    tvEmail.text    = "이메일: ${doc.getString("email") ?: "-"}"
                    tvBirth.text    = "생년월일: ${doc.getString("birth") ?: "-"}"
                    tvGender.text   = "성별: ${doc.getString("gender") ?: "-"}"
                    tvInterest.text = "관심 분야: ${doc.getString("interest") ?: "-"}"
                }
            }
            .addOnFailureListener {
                tvUserName.text = "불러오기 실패"
            }
    }
}
