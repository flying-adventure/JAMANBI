package com.example.jamanbi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
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

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

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

        // 관심분야 목록 불러오기
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

        // 사진 변경
        btnChangePhoto.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                    1001
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    1001
                )
            }

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // 저장 버튼
        btnSave.setOnClickListener {
            val birth = etBirth.text.toString()
            val interest = spInterest.selectedItem.toString()

            if (user != null) {
                val updates = hashMapOf(
                    "birth" to birth,
                    "interest" to interest
                )

                db.collection("users").document(user.uid)
                    .update(updates as Map<String, Any>)
                    .addOnSuccessListener {
                        val resultIntent = Intent()
                        if (imageUri != null) {
                            resultIntent.putExtra("imageUri", imageUri.toString())
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            ivEditProfile.setImageURI(imageUri)
        }
    }
}
