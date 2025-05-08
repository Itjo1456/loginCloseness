package com.example.appdanini.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdanini.data.model.prefs.ClosenessQuestions
import com.example.appdanini.util.TokenManager
import com.example.appdanini.data.model.repository.ClosenessRepository
import com.example.appdanini.data.model.request.closeness.Answer
import com.example.appdanini.data.model.request.closeness.ClosenessAnswerRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClosenessViewModel @Inject constructor(
    private val closenessRepository: ClosenessRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val groupId = tokenManager.getGroupId()

    private val _currentQuestionIndex = MutableLiveData(0)
    val currentQuestionIndex: LiveData<Int> = _currentQuestionIndex

    private val _currentQuestion = MutableLiveData(
        ClosenessQuestions.questions.getOrNull(0) ?: "질문이 없습니다."
    )
    val currentQuestion: LiveData<String> = _currentQuestion

    private val answerList = mutableListOf<Answer>()

    private val _personalScore = MutableLiveData<Int?>()
    val personalScore: LiveData<Int?> = _personalScore

    private val _groupScore = MutableLiveData<Int?>()
    val groupScore: LiveData<Int?> = _groupScore

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _resultReady = MutableLiveData(false)
    val resultReady: LiveData<Boolean> get() = _resultReady

    fun submitAnswer(selectedOptionIndex: Int) {
        val index = _currentQuestionIndex.value ?: 0
        val score = selectedOptionIndex + 1

        answerList.add(
            Answer(
                questionId = index + 1,
                groupId = groupId,
                answer = score
            )
        )

        if (index + 1 < ClosenessQuestions.questions.size) {
            _currentQuestionIndex.value = index + 1
            _currentQuestion.value = ClosenessQuestions.questions[index + 1]
        } else {
            submitAnswersToServer()
        }
    }

    private fun submitAnswersToServer() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val request = ClosenessAnswerRequest(answers = answerList)
                val response = closenessRepository.submitAnswers(request)

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        _personalScore.value = result.personal_score
                        _groupScore.value = result.group_score
                        tokenManager.saveScore(result.group_score, result.personal_score)

                        // ✅ 결과 준비 완료 신호
                        _resultReady.value = true
                    }
                } else {
                    Log.e("ClosenessViewModel", "submitAnswers 실패: ${response.code()} ${response.errorBody()?.string()}")
                    _personalScore.value = null
                    _groupScore.value = null
                }
            } catch (e: Exception) {
                Log.e("ClosenessViewModel", "submitAnswers 예외 발생: ${e.message}")
                _personalScore.value = null
                _groupScore.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}