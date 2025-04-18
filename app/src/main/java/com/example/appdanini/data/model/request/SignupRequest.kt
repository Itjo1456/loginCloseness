package com.example.appdanini.data.model.request

import android.util.Patterns

data class SignupRequest (
    val userName: String?, //회원 email
    val email: String?,
    val password: String?//회원 암호
){
    fun isNotValidEmail() = //이메일 유효성검사
        email.isNullOrBlank()
                || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun isNotValidPassword() = //암호 유효성검사
        password.isNullOrBlank() || password.length !in 8..20
}

