package com.example.jamanbi.signup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.jamanbi.LoginActivity
import com.example.jamanbi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        supportFragmentManager.beginTransaction()
            .replace(R.id.signup_fragment_container, SignupStep1Fragment())
            .commit()
    }

    fun proceedToStep2(email: String) {
        val fragment = SignupStep2Fragment()
        val bundle = Bundle()
        bundle.putString("email", email)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.signup_fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun proceedToStep3(email: String, password: String) {
        val fragment = SignupStep3Fragment()
        val bundle = Bundle()
        bundle.putString("email", email)
        bundle.putString("password", password)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.signup_fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun completeSignup(email: String, password: String, name: String, birth: String, gender: String, interest: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val data = mapOf(
                    "email" to email,
                    "name" to name,
                    "birth" to birth,
                    "gender" to gender,
                    "interest" to interest
                )
                db.collection("user").document(uid)
                    .set(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "회원가입 완료", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "데이터 저장 실패", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
            }
    }
}

class SignupStep1Fragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up_step1, container, false)
        val edtEmail = view.findViewById<EditText>(R.id.edtBandName)
        val btnNext = view.findViewById<Button>(R.id.btnNext)
        btnNext.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            (activity as? SignupActivity)?.proceedToStep2(email)
        }
        return view
    }
}

class SignupStep2Fragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up_step2, container, false)
        val edtPassword = view.findViewById<EditText>(R.id.edtPassword)
        val edtConfirm = view.findViewById<EditText>(R.id.edtConfirmPassword)
        val btnNext = view.findViewById<Button>(R.id.btnNext)
        val email = arguments?.getString("email") ?: ""
        btnNext.setOnClickListener {
            val pw = edtPassword.text.toString()
            val confirm = edtConfirm.text.toString()
            if (pw == confirm) {
                (activity as? SignupActivity)?.proceedToStep3(email, pw)
            } else {
                Toast.makeText(requireContext(), "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
}

class SignupStep3Fragment : Fragment() {

    private lateinit var spinnerInterest: Spinner
    private var selectedInterest: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up_step3, container, false)

        val edtName = view.findViewById<EditText>(R.id.edtName)
        val edtBirth = view.findViewById<EditText>(R.id.edtBirth)
        val btnMale = view.findViewById<Button>(R.id.btnMale)
        val btnFemale = view.findViewById<Button>(R.id.btnFemale)
        spinnerInterest = view.findViewById(R.id.spinner_Interest)
        val btnNext = view.findViewById<Button>(R.id.btnNext)

        var gender = ""
        btnMale.setOnClickListener {
            gender = "남성"
        }
        btnFemale.setOnClickListener {
            gender = "여성"
        }

        fetchInterestOptions()

        btnNext.setOnClickListener {
            val name = edtName.text.toString()
            val birth = edtBirth.text.toString()
            val email = arguments?.getString("email") ?: ""
            val password = arguments?.getString("password") ?: ""
            (activity as? SignupActivity)?.completeSignup(email, password, name, birth, gender, selectedInterest)
        }

        return view
    }

    private fun fetchInterestOptions() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://openapi.q-net.or.kr/")
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .build()

                val service = retrofit.create(QNetService::class.java)
                val response = service.getQualifications("TWJOxOzwAmr4zqg3UL6I0wgvZ6e2sWf0mIHVHW0NMTRmyI0uuvVe2ppK+YCyYLNbKLLbCkSLkvN9vf1vo6/p/A==")

                val list = response.body?.items?.item?.mapNotNull { it.obligfldnm }?.toSet()?.sorted()

                withContext(Dispatchers.Main) {
                    if (!list.isNullOrEmpty()) {
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerInterest.adapter = adapter
                        spinnerInterest.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                selectedInterest = list[position]
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                selectedInterest = ""
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "관심분야 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

interface QNetService {
    @GET("api/service/rest/InquiryListNationalQualificationSVC/getList")
    suspend fun getQualifications(@Query("serviceKey") serviceKey: String): QualificationResponse
}

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
