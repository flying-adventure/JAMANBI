package com.example.jamanbi

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etBirth: EditText
    private lateinit var etMajor: EditText
    private lateinit var spInterest: Spinner
    private lateinit var btnSave: Button
    private lateinit var btnChangePhoto: Button
    private lateinit var ivEditProfile: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // 뷰 초기화
        etBirth = findViewById(R.id.etBirth)
        etMajor = findViewById(R.id.etMajor)
        spInterest = findViewById(R.id.spInterest)
        btnSave = findViewById(R.id.btnSave)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        ivEditProfile = findViewById(R.id.ivEditProfile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Spinner 어댑터 설정
        val options = resources.getStringArray(R.array.interest_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spInterest.adapter = adapter

        // 🔹 Firestore에서 기존 정보 불러오기
        val user = auth.currentUser
        if (user != null) {
            db.collection("user").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        etBirth.setText(doc.getString("birth") ?: "")
                        etMajor.setText(doc.getString("major") ?: "")

                        val interest = doc.getString("interest") ?: options.first()
                        val index = options.indexOf(interest)
                        if (index >= 0) spInterest.setSelection(index)
                    }
                }
        }

        // 🔹 저장 버튼 클릭 → Firestore 업데이트
        btnSave.setOnClickListener {
            val birth = etBirth.text.toString()
            val major = etMajor.text.toString()
            val interest = spInterest.selectedItem.toString()

            if (user != null) {
                val updates = hashMapOf(
                    "birth" to birth,
                    "major" to major,
                    "interest" to interest
                )

                db.collection("user")
                    .document(user.uid)
                    .update(updates as Map<String, Any>)
                    .addOnSuccessListener {
                        setResult(RESULT_OK)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "업데이트 실패", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
