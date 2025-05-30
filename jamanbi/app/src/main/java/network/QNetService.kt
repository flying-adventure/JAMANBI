package com.example.jamanbi.network

import retrofit2.http.GET
import retrofit2.http.Query

interface QNetService {
    @GET("api/service/rest/InquiryListNationalQualificationSVC/getList")
    suspend fun getQualifications(
        @Query("serviceKey") serviceKey: String
    ): QualificationResponse
}
