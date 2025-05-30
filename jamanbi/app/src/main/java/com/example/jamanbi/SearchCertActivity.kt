package com.example.jamanbi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class SearchCertActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var spinner: Spinner
    private val certList = mutableListOf<CertItem>()
    private lateinit var adapter: CertAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchcert)

        recyclerView = findViewById(R.id.recyclerCertList)
        spinner = findViewById(R.id.spinnerObligFld)

        adapter = CertAdapter(certList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_cert
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_cert -> true
                R.id.nav_schedule -> {
                    startActivity(Intent(this, ScheduleActivity::class.java))
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

        fetchCertList()
    }

    private fun fetchCertList() {
        CoroutineScope(Dispatchers.IO).launch {
            val key = "TWJOxOzwAmr4zqg3UL6I0wgvZ6e2sWf0mIHVHW0NMTRmyI0uuvVe2ppK%2BYCyYLNbKLLbCkSLkvN9vf1vo6%2Fp%2FA%3D%3D"
            val url = "https://www.q-net.or.kr/openapi/ncs/majorJobList.do?jmCd=all&jmNm=&qualgbCd=&page=1&perPage=300&dataFormat=xml&svcKey=$key"

            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(response.body?.charStream())

            var eventType = parser.eventType
            var jmfldnm = ""
            var qualgbnm = ""
            var obligfldnm = ""
            val items = mutableListOf<CertItem>()
            val categorySet = mutableSetOf<String>()

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
                            categorySet.add(obligfldnm)
                        }
                    }
                }
                eventType = parser.next()
            }

            runOnUiThread {
                certList.clear()
                certList.addAll(items)
                adapter.notifyDataSetChanged()

                val categoryList = categorySet.toList().sorted()
                val spinnerAdapter = ArrayAdapter(this@SearchCertActivity, android.R.layout.simple_spinner_item, listOf("전체") + categoryList)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = spinnerAdapter

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selected = spinner.selectedItem.toString()
                        val filtered = if (selected == "전체") items else items.filter { it.obligfldnm == selected }
                        certList.clear()
                        certList.addAll(filtered)
                        adapter.notifyDataSetChanged()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
        }
    }
}

data class CertItem(val jmfldnm: String, val qualgbnm: String, val obligfldnm: String)

class CertAdapter(private val list: List<CertItem>) : RecyclerView.Adapter<CertAdapter.CertViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return CertViewHolder(view)
    }

    override fun onBindViewHolder(holder: CertViewHolder, position: Int) {
        val item = list[position]
        holder.title.text = item.jmfldnm
        holder.subtitle.text = "${item.qualgbnm} | ${item.obligfldnm}"
    }

    override fun getItemCount(): Int = list.size

    inner class CertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(android.R.id.text1)
        val subtitle: TextView = view.findViewById(android.R.id.text2)
    }
}
