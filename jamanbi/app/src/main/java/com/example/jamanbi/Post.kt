package com.example.jamanbi

data class Post(
    var id: String? = null,                    // Firestore document ID
    var title: String = "",                    // 게시글 제목
    var content: String = "",                  // 게시글 본문
    var timestamp: Long = 0L,                  // 작성 시각
    var likes: Int = 0,                        // 좋아요 수
    var isExternal: Boolean = false,           // 외부(Naver) 글 여부
    var externalUrl: String = ""               // 외부 링크 URL
)
