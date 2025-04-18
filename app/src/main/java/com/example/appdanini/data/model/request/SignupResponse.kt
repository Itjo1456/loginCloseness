package com.example.appdanini.data.model.request

data class SignupResponse(
    val message: String,
    val userName: String,
    val userId: Long,
    val token: String,
    val refreshToken: String?,
    val inviteUrl: String?
)