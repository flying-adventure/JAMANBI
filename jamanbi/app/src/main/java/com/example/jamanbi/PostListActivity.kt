package com.example.jamanbi

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostListActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var postRecyclerView: RecyclerView
    private val postList = mutableListOf<Post>()

    private lateinit var btnSortLatest: TextView
    private lateinit var btnSortLikes: TextView
    private lateinit var adapter: PostListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)

        firestore = FirebaseFirestore.getInstance()
        postRecyclerView = findViewById(R.id.postRecyclerView)
        postRecyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PostListAdapter(this, postList)
        postRecyclerView.adapter = adapter

        val backButton = findViewById<Button>(R.id.btnBackFromList)
        backButton.setOnClickListener {
            finish()
        }

        btnSortLatest = findViewById(R.id.btnSortLatest)
        btnSortLikes = findViewById(R.id.btnSortLikes)

        btnSortLatest.setOnClickListener {
            updateSortUI(true)
            fetchPosts("timestamp")
        }

        btnSortLikes.setOnClickListener {
            updateSortUI(false)
            fetchPosts("likes")
        }

        // 기본 정렬: 최신순
        updateSortUI(true)
        fetchPosts("timestamp")
    }

    private fun updateSortUI(isLatest: Boolean) {
        btnSortLatest.setTextColor(
            if (isLatest) resources.getColor(android.R.color.holo_blue_dark)
            else resources.getColor(android.R.color.darker_gray)
        )
        btnSortLikes.setTextColor(
            if (!isLatest) resources.getColor(android.R.color.holo_blue_dark)
            else resources.getColor(android.R.color.darker_gray)
        )
    }

    private fun fetchPosts(orderBy: String) {
        postList.clear()

        firestore.collection("posts")
            .orderBy(orderBy, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val post = doc.toObject(Post::class.java)
                    post.id = doc.id
                    postList.add(post)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "게시글 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }
}
