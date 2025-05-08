package com.example.appdanini.data.model.request.auth

// 서버로 요청/응답에 사용되는 데이터 클래스
// # 확인
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)
