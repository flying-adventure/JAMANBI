package com.example.jamanbi

data class Post(
    val title: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val postId: String = "",
    var id: String? = null
)
