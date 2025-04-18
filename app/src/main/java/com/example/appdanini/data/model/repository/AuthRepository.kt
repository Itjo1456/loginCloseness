package com.example.appdanini.data.model.repository

import android.util.Log
import com.example.appdanini.data.model.remote.TokenManager
import com.example.appdanini.data.model.remote.network.AuthApi
import com.example.appdanini.data.model.request.LoginRequest
import com.example.appdanini.data.model.request.LoginResponse
import com.example.appdanini.data.model.request.SignupRequest
import com.example.appdanini.data.model.request.SignupResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {

    suspend fun loginAndGetResponse(email: String, password: String): LoginResponse? {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            Log.d("AuthRepository", "서버 응답 코드: ${response.code()}")
            Log.d("AuthRepository", "서버 응답 바디: ${response.body()?.toString() ?: "null"}")

            if (response.isSuccessful) {
                Log.d("AuthRepository", "loginAndGetResponse 호출됨")
                response.body()?.also { loginResponse ->
                    tokenManager.saveLoginData(
                        loginResponse.token,
                        loginResponse.refreshToken,
                        loginResponse.userName,
                        loginResponse.userId
                    )
                    tokenManager.saveTokensOnly(
                        loginResponse.token,
                        loginResponse.refreshToken
                    )
                    Log.d("AuthRepository", "토큰 저장 완료: ${loginResponse.token}")

                }
            } else {
                Log.e("AuthRepository", "Login failed: ${response.code()} ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login exception: ${e.localizedMessage}")
            null
        }
    }

    suspend fun signup(username: String, email: String, password: String): Boolean {
        return try {
            val response = authApi.signup(SignupRequest(username, email, password))
            Log.d("AuthRepository", "회원가입 응답 코드: ${response.code()}")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("AuthRepository", "Signup exception: ${e.localizedMessage}")
            false
        }
    }

    fun getAccessToken(): String? = tokenManager.getAccessToken()
    fun getRefreshToken(): String? = tokenManager.getRefreshToken()
    fun clearTokens() = tokenManager.clearTokens()
    fun shouldForceLogout(): Boolean = tokenManager.shouldForceLogout()
    fun setForceLogout(value: Boolean) = tokenManager.setForceLogout(value)
    fun clearForceLogoutFlag() = tokenManager.clearForceLogoutFlag()
}