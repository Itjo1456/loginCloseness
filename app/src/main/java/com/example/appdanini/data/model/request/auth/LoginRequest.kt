package com.example.appdanini.data.model.request.auth

data class LoginRequest(
    val email: String, //회원 email
    val password: String//회원 암호
)
