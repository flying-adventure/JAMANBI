package com.example.jamanbi

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
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
            rawContent.take(500) + if (rawContent.length > 500) "..." else ""
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
        val naverBadge = findViewById<TextView>(R.id.naverBadge)

        postTitle.text = title
        backButton.setOnClickListener { finish() }

        if (isExternal) {
            val baseText = previewContent
            val linkText = "\n\n블로그에서 더 보기"
            val spannable = SpannableString(baseText + linkText)
            val start = baseText.length
            val end = spannable.length
            likeButton.visibility = View.GONE
            likeCountView.visibility = View.GONE
            commentEdit.visibility = View.GONE
            commentButton.visibility = View.GONE
            commentListView.visibility = View.GONE



            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    if (externalUrl.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(externalUrl))
                        widget.context.startActivity(intent)
                    }
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = Color.parseColor("#2DB400")
                    ds.typeface = Typeface.DEFAULT_BOLD
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            postContent.text = spannable
            postContent.movementMethod = LinkMovementMethod.getInstance()
        } else {
            postContent.text = previewContent
        }

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
