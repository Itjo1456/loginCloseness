package com.example.appdanini.data.model.repository

import com.example.appdanini.data.model.remote.api.ClosenessApi
import com.example.appdanini.data.model.request.closeness.ClosenessAnswerRequest
import com.example.appdanini.data.model.request.closeness.ClosenessScoreResponse
import retrofit2.Response
import javax.inject.Inject

class ClosenessRepository @Inject constructor(
    private val closenessApi: ClosenessApi
) {
    suspend fun submitAnswers(request: ClosenessAnswerRequest): Response<Unit> {
        return closenessApi.submitAnswers(request)
    }

    suspend fun getClosenessScores(): Response<ClosenessScoreResponse> {
        return closenessApi.getClosenessScores()
    }
}
