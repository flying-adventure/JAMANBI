package com.example.jamanbi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostViewActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private val commentList = mutableListOf<String>()
    private lateinit var commentAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_view)

        firestore = FirebaseFirestore.getInstance()

        var postId = intent.getStringExtra("postId")
        val title = intent.getStringExtra("title") ?: "제목 없음"
        val rawContent = intent.getStringExtra("content") ?: "내용 없음"
        val externalUrl = intent.getStringExtra("externalUrl") ?: ""
        var isExternal = intent.getBooleanExtra("isExternal", false)

        val previewContent = if (isExternal) {
            if (rawContent.length > 150) rawContent.substring(0, 150) + "..." else rawContent
        } else {
            rawContent
        }

        val postTitle = findViewById<TextView>(R.id.postTitle)
        val postContent = findViewById<TextView>(R.id.postContent)
        val backButton = findViewById<TextView>(R.id.backButton)
        val likeCountView = findViewById<TextView>(R.id.likeCount)
        val likeButton = findViewById<Button>(R.id.btnLike)
        val commentEdit = findViewById<EditText>(R.id.editComment)
        val commentButton = findViewById<Button>(R.id.btnSubmitComment)
        val commentListView = findViewById<ListView>(R.id.commentListView)
        val openBlogButton = findViewById<Button>(R.id.btnOpenBlog)
        val naverBadge = findViewById<TextView>(R.id.naverBadge)

        postTitle.text = title
        postContent.text = previewContent
        backButton.setOnClickListener { finish() }

        commentAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, commentList)
        commentListView.adapter = commentAdapter

        val currentUser = FirebaseAuth.getInstance().currentUser

        val targetCollection = if (isExternal) "externalPosts" else "posts"
        val postRef = if (!postId.isNullOrEmpty()) {
            firestore.collection(targetCollection).document(postId)
        } else {
            val doc = firestore.collection("externalPosts").document()
            postId = doc.id
            val postData = hashMapOf(
                "title" to title,
                "content" to previewContent,
                "timestamp" to System.currentTimeMillis(),
                "likes" to 0,
                "isExternal" to true,
                "externalUrl" to externalUrl
            )
            doc.set(postData)
            doc
        }

        postRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val likes = doc.getLong("likes") ?: 0
                likeCountView.text = "좋아요: $likes"
                val externalCheck = doc.getBoolean("isExternal") ?: false
                isExternal = externalCheck
                naverBadge.visibility = if (externalCheck) View.VISIBLE else View.GONE
            }
        }

        if (currentUser != null && postId != null) {
            val userLikeRef = postRef.collection("likedUsers").document(currentUser.uid)

            likeButton.setOnClickListener {
                userLikeRef.get().addOnSuccessListener { docSnapshot ->
                    if (docSnapshot.exists()) {
                        Toast.makeText(this, "이미 좋아요를 누르셨습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        firestore.runTransaction { transaction ->
                            val snapshot = transaction.get(postRef)
                            val currentLikes = snapshot.getLong("likes") ?: 0
                            transaction.update(postRef, "likes", currentLikes + 1)
                            transaction.set(userLikeRef, mapOf("likedAt" to System.currentTimeMillis()))
                        }.addOnSuccessListener {
                            postRef.get().addOnSuccessListener { updatedDoc ->
                                val updatedLikes = updatedDoc.getLong("likes") ?: 0
                                likeCountView.text = "좋아요: $updatedLikes"
                            }
                        }.addOnFailureListener {
                            Toast.makeText(this, "좋아요 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            commentButton.setOnClickListener {
                val commentText = commentEdit.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    val comment = hashMapOf(
                        "content" to commentText,
                        "timestamp" to System.currentTimeMillis()
                    )
                    postRef.collection("comments").add(comment)
                        .addOnSuccessListener {
                            commentEdit.setText("")
                            loadComments(postRef)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "댓글 작성 실패", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            loadComments(postRef)
        }

        if (isExternal) {
            openBlogButton.visibility = View.VISIBLE
            openBlogButton.setOnClickListener {
                if (externalUrl.isNotEmpty()) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(externalUrl))
                    startActivity(browserIntent)
                }
            }
        } else {
            openBlogButton.visibility = View.GONE
        }
    }

    private fun loadComments(postRef: com.google.firebase.firestore.DocumentReference) {
        commentList.clear()
        postRef.collection("comments")
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
