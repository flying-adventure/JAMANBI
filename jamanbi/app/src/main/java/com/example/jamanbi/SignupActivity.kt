package com.example.jamanbi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var interestSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameEdit       = findViewById<EditText>(R.id.editName)
        val birthEdit      = findViewById<EditText>(R.id.editBirth)
        val genderSpinner  = findViewById<Spinner>(R.id.spinnerGender)
        val majorEdit      = findViewById<EditText>(R.id.editMajor)
        interestSpinner    = findViewById(R.id.spinnerInterest)
        val emailEdit      = findViewById<EditText>(R.id.editEmail)
        val passwordEdit   = findViewById<EditText>(R.id.editPassword)
        val signupButton   = findViewById<Button>(R.id.btnSignup)

        // 관심분야 Spinner 데이터 API에서 불러오기
        fetchInterestOptions()

        signupButton.setOnClickListener {
            val name     = nameEdit.text.toString().trim()
            val birth    = birthEdit.text.toString().trim()
            val gender   = genderSpinner.selectedItem as? String ?: ""
            val major    = majorEdit.text.toString().trim()
            val interest = interestSpinner.selectedItem as? String ?: ""
            val email    = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()

            if (name.isEmpty() || birth.length != 6 || major.isEmpty()
                || email.isEmpty() || password.isEmpty()
            ) {
                Toast.makeText(this, "모든 항목을 올바르게 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid
                    if (uid == null) {
                        Toast.makeText(this, "회원가입 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

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

    private fun fetchInterestOptions() {
        lifecycleScope.launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://openapi.q-net.or.kr/")
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .build()

                val service = retrofit.create(QNetService::class.java)
                val response = service.getQualifications(
                    "TWJOxOzwAmr4zqg3UL6I0wgvZ6e2sWf0mIHVHW0NMTRmyI0uuvVe2ppK+YCyYLNbKLLbCkSLkvN9vf1vo6/p/A=="
                )

                val interestList = response.body?.items?.item
                    ?.mapNotNull { it.obligfldnm }
                    ?.toSet()
                    ?.sorted()

                withContext(Dispatchers.Main) {
                    if (!interestList.isNullOrEmpty()) {
                        val adapter = ArrayAdapter(
                            this@SignupActivity,
                            android.R.layout.simple_spinner_item,
                            interestList
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        interestSpinner.adapter = adapter
                    } else {
                        Toast.makeText(this@SignupActivity, "관심 분야 로딩 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignupActivity, "API 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
