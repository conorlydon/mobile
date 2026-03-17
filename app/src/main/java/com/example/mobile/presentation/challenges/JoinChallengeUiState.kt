package com.example.mobile.presentation.challenges

// Represents every possible state the joinchallengeui can be in
// The viewmodel sets the state based on what happens
sealed class JoinChallengeUiState {
    object Idle : JoinChallengeUiState()
    object Loading : JoinChallengeUiState()
    data class Success(val message: String) : JoinChallengeUiState()
    data class Error(val message: String) : JoinChallengeUiState()
} // The compiler knows these are the only states the joinchallengeui can be in