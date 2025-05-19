package com.example.jamanbi

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostListActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var postRecyclerView: RecyclerView
    private val postList = mutableListOf<Post>()

    private lateinit var btnSortLatest: TextView
    private lateinit var btnSortLikes: TextView
    private lateinit var adapter: PostListAdapter

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        // ✅ 드로어 폭 조정
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val drawerWidth = (screenWidth * 0.8).toInt()
        navigationView.layoutParams.width = drawerWidth

        // 🔐 드로어 내 사용자 정보
        val headerView = navigationView.getHeaderView(0)
        val textUserEmail = headerView.findViewById<TextView>(R.id.textUserEmail)
        val btnLogout = headerView.findViewById<Button>(R.id.btnLogout)
        textUserEmail.text = auth.currentUser?.email ?: "비로그인 사용자"
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "로그아웃 완료", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // ☰ 햄버거 버튼
        val btnHamburger = findViewById<Button>(R.id.btnHamburger)
        btnHamburger.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // 🔽 게시글 목록 초기화
        postRecyclerView = findViewById(R.id.postRecyclerView)
        postRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostListAdapter(this, postList)
        postRecyclerView.adapter = adapter

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

        updateSortUI(true)
        fetchPosts("timestamp")

        // ✏️ 글쓰기 FAB
        val fabWritePost = findViewById<FloatingActionButton>(R.id.fabWritePost)
        fabWritePost.setOnClickListener {
            startActivity(Intent(this, PostWriteActivity::class.java))
        }

        // ✅ 하단 BottomNavigationView 처리
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_board

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_cert -> {
                    startActivity(Intent(this, SearchCertActivity::class.java))
                    true
                }
                R.id.nav_schedule -> {
                    startActivity(Intent(this, ScheduleActivity::class.java))
                    true
                }
                R.id.nav_board -> true // 현재 화면
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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
