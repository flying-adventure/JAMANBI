package com.example.jamanbi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jamanbi.repository.InterestRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import android.content.Context


class SearchCertActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var spinner: Spinner
    private lateinit var etSearch: EditText
    private lateinit var adapter: CertAdapter

    private val certList = mutableListOf<CertItem>()
    private var allCertItems = listOf<CertItem>()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchcert)

        recyclerView = findViewById(R.id.recyclerCertList)
        spinner = findViewById(R.id.spinnerObligFld)
        etSearch = findViewById(R.id.etSearch)

        adapter = CertAdapter(this,certList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupBottomNav()
        setupSearchListener()
        fetchUserInterestAndInitSpinner()
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_cert
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_cert -> true
                R.id.nav_fortune -> {
                    startActivity(Intent(this, FortuneActivity::class.java))
                    true
                }
                R.id.nav_board -> {
                    startActivity(Intent(this, PostListActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterCertList()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchUserInterestAndInitSpinner() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val userInterest = doc.getString("interest")
                CoroutineScope(Dispatchers.Main).launch {
                    val interestList = InterestRepository.getInterestList()
                    setupSpinner(interestList, userInterest)
                }
            }
            .addOnFailureListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val interestList = InterestRepository.getInterestList()
                    setupSpinner(interestList, null)
                }
            }
    }

    private fun setupSpinner(categories: List<String>, defaultCategory: String?) {
        val categoryList = listOf("전체") + categories
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner

        if (!defaultCategory.isNullOrBlank()) {
            val index = categoryList.indexOf(defaultCategory)
            if (index >= 0) spinner.setSelection(index)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterCertList()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinner.post {
            spinner.onItemSelectedListener?.onItemSelected(spinner, null, spinner.selectedItemPosition, 0L)
        }

        // Load full list initially
        CoroutineScope(Dispatchers.IO).launch {
            allCertItems = fetchAllCertsFromApi()
            withContext(Dispatchers.Main) { filterCertList() }
        }
    }

    private fun filterCertList() {
        val keyword = etSearch.text.toString().trim()
        val selectedCategory = spinner.selectedItem.toString()
        val filtered = allCertItems.filter {
            (selectedCategory == "전체" || it.obligfldnm == selectedCategory) &&
                    (keyword.isBlank() || it.jmfldnm.contains(keyword, ignoreCase = true))
        }

        certList.clear()
        certList.addAll(filtered)
        adapter.notifyDataSetChanged()
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
        var qualgbnm = ""
        var obligfldnm = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tag = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tag) {
                        "jmfldnm" -> jmfldnm = parser.nextText()
                        "qualgbnm" -> qualgbnm = parser.nextText()
                        "obligfldnm" -> obligfldnm = parser.nextText()
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tag == "item") {
                        items.add(CertItem(jmfldnm, qualgbnm, obligfldnm))
                    }
                }
            }
            eventType = parser.next()
        }

        items
    }
}

data class CertItem(val jmfldnm: String, val qualgbnm: String, val obligfldnm: String)

class CertAdapter(private val context: Context, private val list: List<CertItem>) : RecyclerView.Adapter<CertAdapter.CertViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return CertViewHolder(view)
    }

    override fun onBindViewHolder(holder: CertViewHolder, position: Int) {
        val item = list[position]
        holder.title.text = item.jmfldnm
        holder.subtitle.text = "${item.qualgbnm} | ${item.obligfldnm}"
        holder.itemView.setOnClickListener {
            val intent = Intent(context, BlogPostListActivity::class.java)
            intent.putExtra("keyword", item.jmfldnm)  // 자격증 이름
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size

    inner class CertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(android.R.id.text1)
        val subtitle: TextView = view.findViewById(android.R.id.text2)
    }
}
