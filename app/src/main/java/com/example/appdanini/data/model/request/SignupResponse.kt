package com.example.appdanini.data.model.request

data class SignupResponse(
    val token: String,
    val refreshToken: String, // 이걸 어떻게 주고 받을 것인가
    val userName: String,
    val userId: Long
)


