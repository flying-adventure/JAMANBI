package com.example.jamanbi.viewmodel

import androidx.lifecycle.ViewModel

//DB 구조 따라서 다시 수정

class SignUpViewModel  : ViewModel() {
    var email: String? = null       //이메일
    var password: String? = null    //비밀번호

    var name: String? = null        //이름
    var birth: String? = null       //생년월일
    var gender: String? = null      //성별
}