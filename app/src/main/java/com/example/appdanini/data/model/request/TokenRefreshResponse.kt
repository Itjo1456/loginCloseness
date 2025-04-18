package com.example.appdanini.data.model.request

data class TokenRefreshResponse(
    val token: String,
    val refreshToken: String
)