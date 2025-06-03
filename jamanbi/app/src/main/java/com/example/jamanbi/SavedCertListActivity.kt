package com.example.jamanbi

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class SavedCertListActivity : AppCompatActivity() {

    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerCert: Spinner
    private lateinit var btnAdd: Button
    private lateinit var btnDelete: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedCertAdapter

    private val certItems = mutableListOf<CertItem>()
    private val categoryMap = mutableMapOf<String, MutableList<String>>()
    private val savedList = mutableListOf<SavedItem>()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_cert_list)

        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCert = findViewById(R.id.spinnerCert)
        btnAdd = findViewById(R.id.btnAddCert)
        btnDelete = findViewById(R.id.btndeleteCert)
        recyclerView = findViewById(R.id.recyclerSavedCerts)

        adapter = SavedCertAdapter(savedList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        CoroutineScope(Dispatchers.Main).launch {
            val certs = fetchAllCertsFromApi()
            setupSpinners(certs)
            loadSavedCerts()
        }

        findViewById<TextView>(R.id.backButton3).setOnClickListener {
            finish()
        }

        btnAdd.setOnClickListener {
            val category = spinnerCategory.selectedItem?.toString() ?: return@setOnClickListener
            val cert = spinnerCert.selectedItem?.toString() ?: return@setOnClickListener

            val item = SavedItem(cert, category)
            savedList.add(item)
            adapter.notifyDataSetChanged()

            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("users").document(uid)
                .collection("savedCerts")
                .add(
                    hashMapOf(
                        "name" to cert,
                        "category" to category,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
        }

        btnDelete.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("users").document(uid)
                .collection("savedCerts")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val doc = result.documents[0]
                        val name = doc.getString("name")
                        val category = doc.getString("category")
                        doc.reference.delete()

                        savedList.removeIf { it.name == name && it.category == category }
                        adapter.notifyDataSetChanged()
                        Toast.makeText(this, "가장 최근 자격증 삭제됨", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "삭제할 자격증이 없습니다", Toast.LENGTH_SHORT).show()
                    }
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
        var jmfldnm = ""
        var obligfldnm = ""
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tag = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> when (tag) {
                    "jmfldnm" -> jmfldnm = parser.nextText()
                    "obligfldnm" -> obligfldnm = parser.nextText()
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

        certs.forEach {
            val key = it.obligfldnm.ifBlank { "기타" }
            if (!categoryMap.containsKey(key)) categoryMap[key] = mutableListOf()
            if (!categoryMap[key]!!.contains(it.jmfldnm)) {
                categoryMap[key]!!.add(it.jmfldnm)
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

                val certAdapter = ArrayAdapter(this@SavedCertListActivity, android.R.layout.simple_spinner_item, certList)
                certAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCert.adapter = certAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadSavedCerts() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("savedCerts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                savedList.clear()
                for (doc in result) {
                    val name = doc.getString("name") ?: continue
                    val category = doc.getString("category") ?: continue
                    savedList.add(SavedItem(name, category))
                }
                adapter.notifyDataSetChanged()
            }
    }

    data class CertItem(val jmfldnm: String, val obligfldnm: String)
    data class SavedItem(val name: String, val category: String)
}
