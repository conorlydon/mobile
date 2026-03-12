package com.example.mobile.presentation.challenges

sealed class JoinChallengeUiState {
    object Idle : JoinChallengeUiState()
    object Loading : JoinChallengeUiState()
    data class Success(val message: String) : JoinChallengeUiState()
    data class Error(val message: String) : JoinChallengeUiState()
}