package com.example.appdanini.data.model.request.invite

data class AcceptGroupResponse(
    val invite_request_id: Int,
    val group_id : Int,
    val status : String,
    val createdAt : String
)
