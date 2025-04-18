package com.example.appdanini.data.model.remote.network

import com.example.appdanini.data.model.request.LoginRequest
import com.example.appdanini.data.model.request.LoginResponse
import com.example.appdanini.data.model.request.SignupRequest
import com.example.appdanini.data.model.request.SignupResponse
import com.example.appdanini.data.model.request.TokenRefreshResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("/signup")
    suspend fun signup(@Body req: SignupRequest): Response<SignupResponse>

    @POST("/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    @POST("/refresh")
    suspend fun refreshAccessToken(
        @Header("Authorization") refreshToken: String
    ): Response<TokenRefreshResponse>
}


