package com.example.jamanbi

import android.graphics.Color
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var textViewDday: TextView
    private val examList = mutableListOf<ExamInfo>()

    private val API_URL = "https://api.odcloud.kr/api/15003029/v1/uddi:7b38b228-6028-469e-ace4-f69f9d2573f6"
    private val API_KEY = "TWJOxOzwAmr4zqg3UL6I0wgvZ6e2sWf0mIHVHW0NMTRmyI0uuvVe2ppK%2BYCyYLNbKLLbCkSLkvN9vf1vo6%2Fp%2FA%3D%3D"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarView = findViewById(R.id.calendarView)
        textViewDday = findViewById(R.id.textViewDday)

        calendarView.setOnDateChangedListener { _, date, _ ->
            val clickedDate = String.format("%04d-%02d-%02d", date.year, date.month + 1, date.day)
            val match = examList.find { it.examDate == clickedDate }
            if (match != null) {
                textViewDday.text = "시험명: ${match.name}\n시험일: ${match.examDate}"
            } else {
                textViewDday.text = "해당 날짜에는 시험 일정이 없습니다."
            }
        }

        fetchExamDataFromApi()
    }

    private fun fetchExamDataFromApi() {
        val client = OkHttpClient()
        val url = "$API_URL?serviceKey=$API_KEY"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val jsonObject = JSONObject(responseBody)
                    val dataArray = jsonObject.getJSONArray("data")
                    val dates = mutableListOf<CalendarDay>()
                    examList.clear()

                    for (i in 0 until dataArray.length()) {
                        val obj = dataArray.getJSONObject(i)
                        val name = obj.optString("시험명")
                        val dateStr = obj.optString("시험시작일자")

                        if (dateStr.isNotEmpty()) {
                            val parts = dateStr.split("-")
                            val year = parts[0].toInt()
                            val month = parts[1].toInt() - 1
                            val day = parts[2].toInt()

                            dates.add(CalendarDay.from(year, month, day))
                            examList.add(ExamInfo(name, dateStr))
                        }
                    }

                    runOnUiThread {
                        calendarView.addDecorator(ExamDecorator(dates))
                    }
                }
            }
        })
    }

    data class ExamInfo(val name: String, val examDate: String)
}
