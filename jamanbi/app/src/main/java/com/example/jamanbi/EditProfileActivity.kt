package com.example.jamanbi

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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

        etBirth = findViewById(R.id.etBirth)
        etMajor = findViewById(R.id.etMajor)
        spInterest = findViewById(R.id.spInterest)
        btnSave = findViewById(R.id.btnSave)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        ivEditProfile = findViewById(R.id.ivEditProfile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        fetchInterestOptions()

        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        etBirth.setText(doc.getString("birth") ?: "")
                        etMajor.setText(doc.getString("major") ?: "")
                        val interest = doc.getString("interest")
                        if (interest != null) {
                            val index = (spInterest.adapter as? ArrayAdapter<String>)?.getPosition(interest)
                            if (index != null && index >= 0) {
                                spInterest.setSelection(index)
                            }
                        }
                    }
                }
        }

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
                    ?.distinct()
                    ?.sorted()

                withContext(Dispatchers.Main) {
                    interestList?.let {
                        val finalList = listOf("관심 분야 선택") + it
                        val adapter = ArrayAdapter(
                            this@EditProfileActivity,
                            android.R.layout.simple_spinner_item,
                            finalList
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spInterest.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                Log.e("QNetAPI", "관심분야 API 호출 실패", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "관심 분야 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// Retrofit API 인터페이스
interface QNetService {
    @GET("api/service/rest/InquiryListNationalQualifcationSVC/getList")
    suspend fun getQualifications(
        @Query("serviceKey") serviceKey: String
    ): QualificationResponse
}

// XML 응답 구조
@Root(name = "response", strict = false)
data class QualificationResponse(
    @field:Element(name = "body", required = false)
    var body: QualificationBody? = null
)

@Root(name = "body", strict = false)
data class QualificationBody(
    @field:Element(name = "items", required = false)
    var items: QualificationItems? = null
)

@Root(name = "items", strict = false)
data class QualificationItems(
    @field:ElementList(inline = true, required = false)
    var item: List<QualificationItem>? = null
)

@Root(name = "item", strict = false)
data class QualificationItem(
    @field:Element(name = "obligfldnm", required = false)
    var obligfldnm: String? = null
)
