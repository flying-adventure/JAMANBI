package com.example.jamanbi.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object InterestRepository {
    private var interestList: List<String>? = null

    suspend fun getInterestList(): List<String> = withContext(Dispatchers.IO) {
        if (interestList != null) return@withContext interestList!!

        try {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://openapi.q-net.or.kr/")
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build()

            val service = retrofit.create(QNetService::class.java)
            val response = service.getQualifications(
                "TWJOxOzwAmr4zqg3UL6I0wgvZ6e2sWf0mIHVHW0NMTRmyI0uuvVe2ppK+YCyYLNbKLLbCkSLkvN9vf1vo6/p/A=="
            )

            interestList = response.body?.items?.item
                ?.mapNotNull { it.obligfldnm?.takeIf { name -> name.isNotBlank() } }
                ?.distinct()
                ?.sorted()
                ?: emptyList()

            return@withContext interestList!!
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
}

// Retrofit Service
interface QNetService {
    @GET("api/service/rest/InquiryListNationalQualifcationSVC/getList")
    suspend fun getQualifications(
        @Query("serviceKey") serviceKey: String
    ): QualificationResponse
}

// XML 파싱용 클래스들
@Root(name = "response", strict = false)
data class QualificationResponse(
    @field:Element(name = "body", required = false)
    var body: QualificationBody? = null
)

@Root(name = "body", strict = false)
data class QualificationBody(
    @field:Element(name = "items", required = false)
    var items: QualificationItems? = null
)

@Root(name = "items", strict = false)
data class QualificationItems(
    @field:ElementList(inline = true, required = false)
    var item: List<QualificationItem>? = null
)

@Root(name = "item", strict = false)
data class QualificationItem(
    @field:Element(name = "obligfldnm", required = false)
    var obligfldnm: String? = null
)
