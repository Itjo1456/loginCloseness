package com.example.appdanini.data.model.request.invite

data class AcceptGroupRequest(
    val invite_request_id: String, // 누가 수락을 했는지 확인하기 위함 // 누구의 요청을 수락 보냈는줄 알고
    val approve: Boolean
)
