package com.example.jamanbi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        // 뷰 바인딩
        val nameEdit       = findViewById<EditText>(R.id.editName)
        val birthEdit      = findViewById<EditText>(R.id.editBirth)
        val genderSpinner  = findViewById<Spinner>(R.id.spinnerGender)
        val majorEdit      = findViewById<EditText>(R.id.editMajor)
        val interestSpinner= findViewById<Spinner>(R.id.spinnerInterest)
        val emailEdit      = findViewById<EditText>(R.id.editEmail)
        val passwordEdit   = findViewById<EditText>(R.id.editPassword)
        val signupButton   = findViewById<Button>(R.id.btnSignup)

        signupButton.setOnClickListener {
            //입력값 읽어오기
            val name     = nameEdit.text.toString().trim()
            val birth    = birthEdit.text.toString().trim()
            val gender   = genderSpinner.selectedItem as? String ?: ""
            val major    = majorEdit.text.toString().trim()
            val interest = interestSpinner.selectedItem as? String ?: ""
            val email    = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()

            // 검증
            if (name.isEmpty() || birth.length != 6 || major.isEmpty()
                || email.isEmpty() || password.isEmpty()
            ) {
                Toast.makeText(this, "모든 항목을 올바르게 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3) Firebase Auth 회원 생성
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid
                    if (uid == null) {
                        Toast.makeText(this, "회원가입 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Firestore에 프로필 정보 저장
                    val userData = mapOf(
                        "name"     to name,
                        "birth"    to birth,
                        "gender"   to gender,
                        "major"    to major,
                        "interest" to interest,
                        "email"    to email
                    )
                    db.collection("user").document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show()
                            // 로그인 화면으로 이동
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "정보 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "회원가입 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
