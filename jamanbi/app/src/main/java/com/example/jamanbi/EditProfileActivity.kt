package com.example.jamanbi

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

    companion object {
        private const val PREFS = "UserPrefs"
    }

    private lateinit var etMajor: EditText
    private lateinit var spInterest: Spinner
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)


        etMajor     = findViewById(R.id.etMajor)
        spInterest  = findViewById(R.id.spInterest)
        btnSave     = findViewById(R.id.btnSave)

        // 1) Spinner 어댑터 세팅
        val options = resources.getStringArray(R.array.interest_options)
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            options).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spInterest.adapter = adapter

        // 2) 기존 값 불러오기
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        etMajor.setText(prefs.getString("userMajor",""))

        // spinner 위치를 기존 값과 일치시켜 선택
        val savedInterest = prefs.getString("userInterest", options.first())
        val index = options.indexOf(savedInterest)
        if (index >= 0) spInterest.setSelection(index)

        // 3) 저장 버튼 클릭
        btnSave.setOnClickListener {
            prefs.edit()
                .putString("userMajor", etMajor.text.toString())
                .putString("userInterest", spInterest.selectedItem as String)
                .apply()

            setResult(RESULT_OK)
            finish()
        }
    }
}
