package com.example.jamanbi.SignUp

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.jamanbi.MainActivity
import com.example.jamanbi.R
import com.example.jamanbi.viewmodel.SignUpViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpStep3Fragment : Fragment() {
    lateinit var btnNext: Button
    lateinit var edtName: EditText
    lateinit var edtBirth: EditText
    lateinit var btnMale: Button
    lateinit var btnFemale: Button
    lateinit var gender: String   // 전역 변수로 선언
    private val signUpViewModel: SignUpViewModel by activityViewModels() // ViewModel 공유

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        //스텝3을 컨테이너에 띄우도록 view로 설정
        val view = inflater.inflate(R.layout.fragment_sign_up_step3, container, false)
        auth = FirebaseAuth.getInstance()

        btnNext = view.findViewById(R.id.btnNext)
        edtName = view.findViewById(R.id.edtName)
        edtBirth = view.findViewById(R.id.edtBirth)
        btnMale = view.findViewById(R.id.btnMale)
        btnFemale = view.findViewById(R.id.btnFemale)



        //사용자가 누른 버튼에 따라 gender의 String 값 설정
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

            //TODO : 값이 입력되지 않은 상태일 때 모달/문구 띄우기 & 다음 스텝 넘어가지 못하게

            val name = edtName.text.toString()
            val birth = edtBirth.text.toString()

            Log.d("디버깅", "정보 - 이름 : $name, 생년월일 : $birth, 성별 : $gender")

            if (name.isBlank() || birth.isBlank() || !::gender.isInitialized) {
                Toast.makeText(requireContext(), "모든 값을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            //viewModel에 이름, 생년월일, 성별 저장
            signUpViewModel.name = name
            signUpViewModel.birth = birth
            signUpViewModel.gender = gender


            //TODO: 가입 + FireBase연동

            val email = signUpViewModel.email
            val password = signUpViewModel.password


            //null check
            if (email.isNullOrBlank() || password.isNullOrBlank()) {
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                    val userInfo = hashMapOf(
                        "name" to name,
                        "birth" to birth,
                        "gender" to gender,
                        "email" to email
                    )
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .set(userInfo)
                        .addOnSuccessListener {
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            activity?.finish()
                        }
                    Toast.makeText(requireContext(), "회원가입 성공", Toast.LENGTH_SHORT).show()
                    // 여기서 로그인 화면이나 메인 화면으로 이동
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    activity?.finish()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "회원가입 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }

        }
        return view
    }
    private lateinit var auth: FirebaseAuth

}