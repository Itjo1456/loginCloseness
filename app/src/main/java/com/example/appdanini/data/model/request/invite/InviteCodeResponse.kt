package com.example.appdanini.data.model.request.invite

// 그룹 아이디
// 초대 코드
// # 확인
data class InviteCodeResponse(
    val group_id : Int,
    val inviteCode : String,
    val createdAt : String
)
