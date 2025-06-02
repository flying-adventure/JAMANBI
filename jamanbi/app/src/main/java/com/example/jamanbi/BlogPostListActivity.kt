package com.example.jamanbi

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jamanbi.naverapi.BlogPost
import com.example.jamanbi.naverapi.NaverBlogApiService
import com.example.jamanbi.naverapi.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BlogPostListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostListAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list) // ✅ 기존 레이아웃 재사용
        val fab = findViewById<View>(R.id.fabWritePost)
        fab.visibility = View.GONE


        findViewById<View>(R.id.btnSortLatest).visibility = View.GONE
        findViewById<View>(R.id.btnSortLikes).visibility = View.GONE
        findViewById<View>(R.id.btnHamburger).visibility = View.GONE



        recyclerView = findViewById(R.id.postRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostListAdapter(this, postList, showLikesAndComments = false)
        recyclerView.adapter = adapter

        val keyword = intent.getStringExtra("keyword") ?: return
        val keywordText = findViewById<TextView>(R.id.textSearchKeyword)
        keywordText.text = "[$keyword] 검색 결과"
        keywordText.visibility = View.VISIBLE

        fetchBlogPosts(keyword)
    }

    private fun fetchBlogPosts(keyword: String) {
        val clientId = "SGXSwMfIbT6ZMwOBCXVw"
        val clientSecret = "Hqb6m6KDJJ"
        val api = RetrofitClient.create(clientId, clientSecret)

        api.searchBlogs(keyword).enqueue(object : Callback<com.example.jamanbi.naverapi.NaverBlogResponse> {
            override fun onResponse(call: Call<com.example.jamanbi.naverapi.NaverBlogResponse>, response: Response<com.example.jamanbi.naverapi.NaverBlogResponse>) {
                if (response.isSuccessful) {
                    val blogPosts = response.body()?.items ?: emptyList()
                    postList.clear()
                    postList.addAll(blogPosts.map {
                        Post(
                            title = it.title.replace(Regex("<.*?>"), ""), // HTML 제거
                            content = it.description.replace(Regex("<.*?>"), ""),
                            timestamp = System.currentTimeMillis(),
                            likes = 0,
                            isExternal = true,
                            externalUrl = it.link
                        )
                    })
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@BlogPostListActivity, "API 응답 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.example.jamanbi.naverapi.NaverBlogResponse>, t: Throwable) {
                Toast.makeText(this@BlogPostListActivity, "API 요청 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
