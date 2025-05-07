package com.example.jamanbi

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View


class PostWriteActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_write)

        firestore = FirebaseFirestore.getInstance()

        val titleEdit = findViewById<EditText>(R.id.editTitle)
        val contentEdit = findViewById<EditText>(R.id.editContent)
        val submitButton = findViewById<Button>(R.id.btnSubmit)

        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerSubCategory = findViewById<Spinner>(R.id.spinnerSubCategory)
        val spinnerCertificate = findViewById<Spinner>(R.id.spinnerCertificate)


        val categoryMap = mapOf(
            "[분야를 선택하세요]" to listOf(""),
            "어학" to listOf("[언어를 선택하세요]","영어", "일본어", "중국어", "스페인어"),
            "기사자격증" to listOf("[업종을 선택하세요]","안전", "요식업", "건설", "설비", "전기"),
            "한국사" to listOf("[난이도를 선택하세요]","기본", "심화"),
            "정보" to listOf("[분류를 선택하세요]","정보관리", "정보보안", "정보처리", "컴퓨터", "미디어 컨텐츠")
        )

        val certMap = mapOf(
            "자격증을 선택하세요(소분류)" to listOf("[자격증을 선택하세요]"),
            "영어" to listOf("[자격증을 선택하세요]", "TOEIC", "TOEFL", "OPIC", "TEPS", "IELTS"),
            "일본어" to listOf("[자격증을 선택하세요]", "JLPT N1", "JLPT N2", "JPT"),
            "중국어" to listOf("[자격증을 선택하세요]", "HSK 6급", "HSK 5급", "BCT"),
            "스페인어" to listOf("[자격증을 선택하세요]", "DELE B2", "DELE C1", "SIELE"),
            "안전" to listOf("[자격증을 선택하세요]", "산업안전기사", "건설안전기사", "소방설비기사"),
            "요식업" to listOf("[자격증을 선택하세요]", "조리기능사(한식)", "조리기능사(양식)", "조리기능사(중식)"),
            "건설" to listOf("[자격증을 선택하세요]", "건축기사", "토목기사", "측량및지형공간정보기사"),
            "설비" to listOf("[자격증을 선택하세요]", "공조냉동기계기사", "에너지관리기사", "가스기사"),
            "전기" to listOf("[자격증을 선택하세요]", "전기기사", "전기공사기사", "전기기능사"),
            "기본" to listOf("[자격증을 선택하세요]", "한국사능력검정시험 3급", "한국사능력검정시험 4급"),
            "심화" to listOf("[자격증을 선택하세요]", "한국사능력검정시험 1급", "한국사능력검정시험 2급"),
            "정보관리" to listOf("[자격증을 선택하세요]", "정보기술자격(ITQ)", "전자상거래관리사", "ERP 정보관리사"),
            "정보보안" to listOf("[자격증을 선택하세요]", "정보보안기사", "정보보호산업기사", "CISSP"),
            "정보처리" to listOf("[자격증을 선택하세요]", "정보처리기사", "정보처리산업기사", "정보처리기능사"),
            "컴퓨터" to listOf("[자격증을 선택하세요]", "컴퓨터활용능력 1급", "컴퓨터활용능력 2급", "워드프로세서"),
            "미디어 컨텐츠" to listOf("[자격증을 선택하세요]", "멀티미디어콘텐츠제작전문가", "GTQ", "디지털영상편집")
        )

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryMap.keys.toList())
        spinnerCategory.adapter = categoryAdapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = categoryMap.keys.toList()[position]
                val subList = categoryMap[selectedCategory] ?: listOf()
                spinnerSubCategory.adapter = ArrayAdapter(this@PostWriteActivity, android.R.layout.simple_spinner_item, subList)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerSubCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSub = spinnerSubCategory.selectedItem as? String ?: ""
                val certList = certMap[selectedSub] ?: listOf()
                spinnerCertificate.adapter = ArrayAdapter(this@PostWriteActivity, android.R.layout.simple_spinner_item, certList)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 🔹 게시글 업로드
        submitButton.setOnClickListener {
            val title = titleEdit.text.toString()
            val content = contentEdit.text.toString()
            val cert = spinnerCertificate.selectedItem as? String
            val finalTitle = if (!cert.isNullOrEmpty() && cert != "[자격증을 선택하세요]") "[$cert] $title" else title


            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val post = hashMapOf(
                "title" to finalTitle,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "likes" to 0
            )

            firestore.collection("posts")
                .add(post)
                .addOnSuccessListener {
                    Toast.makeText(this, "🎉 게시글이 성공적으로 등록되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "❌ 게시글 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
