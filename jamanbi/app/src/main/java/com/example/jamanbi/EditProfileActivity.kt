package com.example.jamanbi

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class EditProfileActivity : AppCompatActivity() {

    companion object {
        private const val PREFS = "UserPrefs"
        private const val REQUEST_CODE_PICK_IMAGE = 101
    }

    private lateinit var ivEditProfile: ImageView
    private lateinit var btnChangePhoto: Button
    private lateinit var etBirth: EditText
    private lateinit var etMajor: EditText
    private lateinit var spInterest: Spinner
    private lateinit var btnSave: Button

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        ivEditProfile = findViewById(R.id.ivEditProfile)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        etBirth = findViewById(R.id.etBirth)
        etMajor = findViewById(R.id.etMajor)
        spInterest = findViewById(R.id.spInterest)
        btnSave = findViewById(R.id.btnSave)

        val options = resources.getStringArray(R.array.interest_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spInterest.adapter = adapter

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        etBirth.setText(prefs.getString("userBirth", ""))
        etMajor.setText(prefs.getString("userMajor", ""))
        val savedInterest = prefs.getString("userInterest", options.first())
        val index = options.indexOf(savedInterest)
        if (index >= 0) spInterest.setSelection(index)

        // Load saved profile image if exists
        val savedImageUri = prefs.getString("profileImageUri", null)
        savedImageUri?.let {
            ivEditProfile.setImageURI(Uri.parse(it))
        }

        btnChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }

        btnSave.setOnClickListener {
            prefs.edit {
                putString("userBirth", etBirth.text.toString())
                putString("userMajor", etMajor.text.toString())
                putString("userInterest", spInterest.selectedItem as String)
                selectedImageUri?.let {
                    putString("profileImageUri", it.toString())
                }
            }
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let {
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                ivEditProfile.setImageBitmap(bitmap)
            }
        }
    }
}
