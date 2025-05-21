package com.example.jamanbi.SignUp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.jamanbi.R
import com.example.jamanbi.viewmodel.SignUpViewModel
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.jamanbi.SignUp.SignUp
import com.example.jamanbi.SignUp.SignUpStep3Fragment


class SignUpStep2Fragment : Fragment () {

    lateinit var btnNext: Button
    lateinit var edtPassword: EditText
    lateinit var edtConfirmPassword: EditText
    private val signUpViewModel: SignUpViewModel by activityViewModels() // ViewModel 공유

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //스텝3을 컨테이너에 띄우도록 view로 설정
        val view = inflater.inflate(R.layout.fragment_sign_up_step2, container, false)

        btnNext = view.findViewById(R.id.btnNext)
        edtPassword = view.findViewById(R.id.edtPassword)
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword)

        btnNext.setOnClickListener {

            //TODO : 값이 입력되지 않은 상태일 때 모달/문구 띄우기 & 다음 스텝 넘어가지 못하게

            val password = edtPassword.text.toString()
            val confirmPassword = edtConfirmPassword.text.toString()

            if (password == confirmPassword) {
                Log.d("디버깅", "비밀번호 : $password -> 비밀번호 생성완료")

                //viewModel에 비밀번호 저장
                signUpViewModel.password = password

                //다음 스텝 레이아웃으로 이동
                (activity as? SignUp)?.goToNextFragment(SignUpStep3Fragment())
            }
        }
        return view
    }
}

