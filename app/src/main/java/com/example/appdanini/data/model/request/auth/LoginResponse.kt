package com.example.appdanini.data.model.request.auth

data class LoginResponse(
    val email: String, //회원 email
    val name: String,//회원 암호
    val groupId : Int?
)
