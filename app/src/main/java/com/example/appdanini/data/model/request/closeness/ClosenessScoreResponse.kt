package com.example.appdanini.data.model.request.closeness


// 서버에서 점수를 받을 때 사용하는 데이터 모델
//
//kotlin
//복사
//편집
data class ClosenessScoreResponse (
    val group_score : Int,
    val personal_score : Int
)