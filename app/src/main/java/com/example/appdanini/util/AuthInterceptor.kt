package com.example.appdanini.util

import com.example.appdanini.data.model.remote.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {   //chain 객체는 현재의 요청 흐름(Context)입니다. Response는 서버로부터의 응답 결과를 의미합니다.
        val accessToken = tokenManager.getAccessToken()

        // 원래 요청을 복사해서, 새로운 요청을 만든다.
        val newRequest = chain.request().newBuilder().apply {
            accessToken?.let { // 토큰이 null이 아니라면, 해더에 추가
                addHeader("Authorization", "Bearer $it")
            }
        }.build()

        return chain.proceed(newRequest)
    }
}
