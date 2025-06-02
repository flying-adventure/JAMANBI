package com.example.jamanbi

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.jamanbi.repository.InterestRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etBirth: EditText
    private lateinit var spInterest: Spinner
    private lateinit var btnSave: Button
    private lateinit var btnChangePhoto: Button
    private lateinit var ivEditProfile: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        etBirth = findViewById(R.id.etBirth)
        spInterest = findViewById(R.id.spInterest)
        btnSave = findViewById(R.id.btnSave)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        ivEditProfile = findViewById(R.id.ivEditProfile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 관심분야 목록 불러오기 (공통 Repository 사용)
        loadInterestList()

        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        etBirth.setText(doc.getString("birth") ?: "")
                        val interest = doc.getString("interest")
                        val adapter = spInterest.adapter as? ArrayAdapter<String>
                        if (interest != null && adapter != null) {
                            val index = adapter.getPosition(interest)
                            if (index >= 0) {
                                spInterest.setSelection(index)
                            }
                        }
                    }
                }
        }

        btnSave.setOnClickListener {
            val birth = etBirth.text.toString()
            val interest = spInterest.selectedItem.toString()

            if (user != null) {
                val updates = hashMapOf(
                    "birth" to birth,
                    "interest" to interest
                )

                db.collection("users")
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

    private fun loadInterestList() {
        lifecycleScope.launch {
            val interestList = InterestRepository.getInterestList()
            val finalList = listOf("관심 분야 선택") + interestList
            val adapter = ArrayAdapter(
                this@EditProfileActivity,
                android.R.layout.simple_spinner_item,
                finalList
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            spInterest.adapter = adapter
        }
    }
}
