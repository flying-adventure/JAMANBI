package com.example.jamanbi.naverapi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverBlogApiService {
    @GET("v1/search/blog.json")
    fun searchBlogs(
        @Query("query") query: String,
        @Query("display") display: Int = 5,
        @Query("sort") sort: String = "date"
    ): Call<NaverBlogResponse>
}
