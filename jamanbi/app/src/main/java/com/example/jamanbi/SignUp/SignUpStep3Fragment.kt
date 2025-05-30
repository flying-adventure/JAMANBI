package com.example.jamanbi.SignUp

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.jamanbi.MainActivity
import com.example.jamanbi.R
import com.example.jamanbi.viewmodel.SignUpViewModel
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

class SignUpStep3Fragment : Fragment() {
    private lateinit var btnNext: Button
    private lateinit var edtName: EditText
    private lateinit var edtBirth: EditText
    private lateinit var btnMale: Button
    private lateinit var btnFemale: Button
    private lateinit var gender: String
    private lateinit var interestSpinner: Spinner
    private lateinit var auth: FirebaseAuth
    private val signUpViewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sign_up_step3, container, false)
        auth = FirebaseAuth.getInstance()

        btnNext = view.findViewById(R.id.btnNext)
        edtName = view.findViewById(R.id.edtName)
        edtBirth = view.findViewById(R.id.edtBirth)
        btnMale = view.findViewById(R.id.btnMale)
        btnFemale = view.findViewById(R.id.btnFemale)
        interestSpinner = view.findViewById(R.id.spinner_Interest)

        fetchInterestOptions()

        btnMale.setOnClickListener {
            btnMale.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.lightLight))
            btnMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.mediumDark))
            btnFemale.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.mediumLight))
            btnFemale.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightestDark))
            gender = "Male"
        }

        btnFemale.setOnClickListener {
            btnMale.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.mediumLight))
            btnMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightestDark))
            btnFemale.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.lightLight))
            btnFemale.setTextColor(ContextCompat.getColor(requireContext(), R.color.mediumDark))
            gender = "Female"
        }

        btnNext.setOnClickListener {
            val name = edtName.text.toString()
            val birth = edtBirth.text.toString()
            val interest = interestSpinner.selectedItem as? String ?: ""

            if (name.isBlank() || birth.isBlank() || !::gender.isInitialized) {
                Toast.makeText(requireContext(), "모든 값을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signUpViewModel.name = name
            signUpViewModel.birth = birth
            signUpViewModel.gender = gender
            signUpViewModel.interest = interest

            val email = signUpViewModel.email
            val password = signUpViewModel.password

            if (email.isNullOrBlank() || password.isNullOrBlank()) return@setOnClickListener

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                    val userInfo = hashMapOf(
                        "name" to name,
                        "birth" to birth,
                        "gender" to gender,
                        "interest" to interest,
                        "email" to email
                    )
                    FirebaseFirestore.getInstance().collection("users").document(uid).set(userInfo)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "회원가입 성공", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            activity?.finish()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "회원가입 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
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
                val response = service.getQualifications(
                    "TWJOxOzwAmr4zqg3UL6I0wgvZ6e2sWf0mIHVHW0NMTRmyI0uuvVe2ppK+YCyYLNbKLLbCkSLkvN9vf1vo6/p/A=="
                )

                val interestList = response.body?.items?.item
                    ?.mapNotNull { it.obligfldnm }
                    ?.distinct()
                    ?.sorted()

                withContext(Dispatchers.Main) {
                    interestList?.let {
                        val finalList = listOf("관심 분야 선택") + it
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            finalList
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        interestSpinner.adapter = adapter
                    } ?: run {
                        Toast.makeText(requireContext(), "관심 분야 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("QNetAPI", "API 호출 실패", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "관심 분야 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

interface QNetService {
    @GET("api/service/rest/InquiryListNationalQualifcationSVC/getList")
    suspend fun getQualifications(
        @Query("serviceKey") serviceKey: String
    ): QualificationResponse
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
