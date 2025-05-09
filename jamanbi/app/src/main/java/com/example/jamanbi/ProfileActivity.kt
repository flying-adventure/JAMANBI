package com.example.jamanbi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val REQ_EDIT_PROFILE = 100
    }

    private lateinit var tvUserName: TextView
    private lateinit var tvMajor: TextView
    private lateinit var tvInterest: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 뷰 바인딩
        tvUserName = findViewById(R.id.tvUserName)
        tvMajor    = findViewById(R.id.tvMajor)
        tvInterest = findViewById(R.id.tvInterest)

        // 초기 프로필 로드
        loadProfileFromPrefs()

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            //SharedPreferences 초기화
            val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            prefs.edit().clear().apply()

            // LoginActivity로 이동하면서 기존 스택을 모두 지워서
            //    뒤로 가기 시 프로필 화면이 안 보이게 함
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            // 현재 액티비티도 종료
            finish()
        }


        findViewById<Button>(R.id.btnEditInfo).setOnClickListener {
            // 수정 화면을 결과 처리(RequestCode)와 함께 호출
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivityForResult(intent, REQ_EDIT_PROFILE)
        }

        findViewById<Button>(R.id.btnSavedCerts).setOnClickListener {
            startActivity(Intent(this, SavedCertListActivity::class.java))
        }

        findViewById<Button>(R.id.btnWithdraw).setOnClickListener {
            // 회원탈퇴 기능 구현
        }

        // BottomNavigationView 클릭 처리
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_cert -> {
                    startActivity(Intent(this, SearchCertActivity::class.java))
                    true
                }
                R.id.nav_schedule -> {
                    startActivity(Intent(this, ScheduleActivity::class.java))
                    true
                }
                R.id.nav_board -> {
                    startActivity(Intent(this, PostListActivity::class.java))
                    true
                }
                R.id.nav_profile -> true // 현재 페이지
                else -> false
            }
        }
    }

    // EditProfileActivity에서 돌아올 때 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_EDIT_PROFILE && resultCode == RESULT_OK) {
            // 수정 완료 후, SharedPreferences에서 최신 정보 다시 로드
            loadProfileFromPrefs()
        }
    }

    // SharedPreferences에서 프로필 읽어와 화면에 반영
    private fun loadProfileFromPrefs() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val name     = prefs.getString("userName", "이름 없음")
        val major    = prefs.getString("userMajor", "전공 없음")
        val interest = prefs.getString("userInterest", "관심 없음")

        tvUserName.text = name
        tvMajor   .text = "전공: $major"
        tvInterest.text = "관심 분야: $interest"
    }
}
