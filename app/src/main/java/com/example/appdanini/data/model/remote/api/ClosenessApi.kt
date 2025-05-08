package com.example.appdanini.data.model.remote.api

import com.example.appdanini.data.model.request.closeness.ClosenessAnswerRequest
import com.example.appdanini.data.model.request.closeness.ClosenessScoreResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

//Retrofit 통신용 API가 아니라 "Repository랑 ViewModel 연결하기 위한 추상 인터페이스" 였어

interface ClosenessApi {
    @POST("/api/v1/tests/answers")
    suspend fun submitAnswers(
        @Body request: ClosenessAnswerRequest
    ): Response<ClosenessScoreResponse>
}

//
//서버로 답변 목록을 한 번에 제출하는 기능만 존재.
//Retrofit2 방식으로 API를 정의했어.
//요청 본문은 ClosenessAnswerRequest 형태이고, 응답은 ClosenessScoreResponse(= 점수).