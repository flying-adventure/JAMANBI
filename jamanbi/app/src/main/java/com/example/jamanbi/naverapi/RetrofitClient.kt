package com.example.jamanbi.naverapi

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://openapi.naver.com/"

    fun create(clientId: String, clientSecret: String): NaverBlogApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request = chain.request().newBuilder()
                        .addHeader("X-Naver-Client-Id", clientId)
                        .addHeader("X-Naver-Client-Secret", clientSecret)
                        .build()
                    return chain.proceed(request)
                }
            })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(NaverBlogApiService::class.java)
    }
}
