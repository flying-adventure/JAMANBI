package com.example.jamanbi

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class PostViewActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private val commentList = mutableListOf<String>()
    private lateinit var commentAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_view)

        firestore = FirebaseFirestore.getInstance()

        val postId = intent.getStringExtra("postId")
        val title = intent.getStringExtra("title") ?: "제목 없음"
        val content = intent.getStringExtra("content") ?: "내용 없음"

        findViewById<TextView>(R.id.postTitle).text = "$title"
        findViewById<TextView>(R.id.postContent).text = content

        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish() // 뒤로가기
        }

        val likeCountView = findViewById<TextView>(R.id.likeCount)
        val likeButton = findViewById<Button>(R.id.btnLike)

        if (postId != null) {
            val postRef = firestore.collection("posts").document(postId)

            // 초기 좋아요 수 표시
            postRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val likes = doc.getLong("likes") ?: 0
                    likeCountView.text = "좋아요: $likes"
                }
            }

            likeButton.setOnClickListener {
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    val currentLikes = snapshot.getLong("likes") ?: 0
                    transaction.update(postRef, "likes", currentLikes + 1)
                    currentLikes + 1
                }.addOnSuccessListener { updatedLikes ->
                    likeCountView.text = "좋아요: $updatedLikes"
                }.addOnFailureListener {
                    Toast.makeText(this, "좋아요 실패", Toast.LENGTH_SHORT).show()
                }
            }
            loadComments(postId) // 화면 진입 시 기존 댓글 불러오기

        }
        val commentEdit = findViewById<EditText>(R.id.editComment)
        val commentButton = findViewById<Button>(R.id.btnSubmitComment)
        val commentListView = findViewById<ListView>(R.id.commentListView)

        commentAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, commentList)
        commentListView.adapter = commentAdapter


// 🔸 댓글 등록

        commentButton.setOnClickListener {
            val commentText = commentEdit.text.toString().trim()
            if (commentText.isNotEmpty() && postId != null) {
                val comment = hashMapOf(
                    "content" to commentText,
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("posts").document(postId).collection("comments")
                    .add(comment)
                    .addOnSuccessListener {
                        commentEdit.setText("") // 입력칸 비우기
                        loadComments(postId) // 새로고침
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "댓글 작성 실패", Toast.LENGTH_SHORT).show()
                    }
            }
        }

    }
    private fun loadComments(postId: String) {
        commentList.clear()

        firestore.collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val text = doc.getString("content") ?: ""
                    commentList.add(text)
                }
                commentAdapter.notifyDataSetChanged()
            }
    }

}
