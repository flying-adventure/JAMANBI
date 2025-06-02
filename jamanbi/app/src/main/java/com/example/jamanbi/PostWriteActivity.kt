package com.example.jamanbi

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class PostWriteActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerCert: Spinner
    private val certItems = mutableListOf<CertItem>()
    private val categoryMap = mutableMapOf<String, MutableList<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_write)

        firestore = FirebaseFirestore.getInstance()

        val titleEdit = findViewById<EditText>(R.id.editTitle)
        val contentEdit = findViewById<EditText>(R.id.editContent)
        val submitButton = findViewById<Button>(R.id.btnSubmit)

        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCert = findViewById(R.id.spinnerCert)

        CoroutineScope(Dispatchers.Main).launch {
            val certs = fetchAllCertsFromApi()
            setupSpinners(certs)
        }

        submitButton.setOnClickListener {
            val title = titleEdit.text.toString()
            val content = contentEdit.text.toString()
            val selectedCert = spinnerCert.selectedItem?.toString()?.takeIf { it.isNotEmpty() } ?: ""
            val finalTitle = if (selectedCert.isNotBlank()) "[$selectedCert] $title" else title

            if (title.isBlank() || content.isBlank()) {
                Toast.makeText(this, "제목과 내용을 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val post = hashMapOf(
                "title" to finalTitle,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "likes" to 0,
                "email" to FirebaseAuth.getInstance().currentUser?.email
            )

            firestore.collection("posts")
                .add(post)
                .addOnSuccessListener {
                    Toast.makeText(this, "🎉 게시글 등록 완료", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "❌ 업로드 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private suspend fun fetchAllCertsFromApi(): List<CertItem> = withContext(Dispatchers.IO) {
        val key = "TWJOxOzwAmr4zqg3UL6I0wgvZ6e2sWf0mIHVHW0NMTRmyI0uuvVe2ppK+YCyYLNbKLLbCkSLkvN9vf1vo6/p/A=="
        val url = "http://openapi.q-net.or.kr/api/service/rest/InquiryListNationalQualifcationSVC/getList?serviceKey=$key"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(response.body?.charStream())

        val items = mutableListOf<CertItem>()
        var eventType = parser.eventType
        var jmfldnm = ""
        var obligfldnm = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tag = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tag) {
                        "jmfldnm" -> jmfldnm = parser.nextText()
                        "obligfldnm" -> obligfldnm = parser.nextText()
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tag == "item") {
                        items.add(CertItem(jmfldnm, obligfldnm))
                        jmfldnm = ""
                        obligfldnm = ""
                    }
                }
            }
            eventType = parser.next()
        }
        items
    }

    private fun setupSpinners(certs: List<CertItem>) {
        certItems.clear()
        certItems.addAll(certs)

        certs.forEach { item ->
            val key = item.obligfldnm.ifBlank { "기타" }
            if (!categoryMap.containsKey(key)) categoryMap[key] = mutableListOf()
            if (!categoryMap[key]!!.contains(item.jmfldnm)) {
                categoryMap[key]!!.add(item.jmfldnm)
            }
        }

        val categoryList = listOf("전체") + categoryMap.keys.sorted()
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedCategory = categoryList[position]
                val certList = if (selectedCategory == "전체") {
                    certItems.map { it.jmfldnm }.distinct().sorted()
                } else {
                    categoryMap[selectedCategory]?.sorted() ?: emptyList()
                }

                val certAdapter = ArrayAdapter(this@PostWriteActivity, android.R.layout.simple_spinner_item, certList)
                certAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCert.adapter = certAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    data class CertItem(val jmfldnm: String, val obligfldnm: String)
}
