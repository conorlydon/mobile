package com.example.mobile.presentation.challenges

// Tracks the lifecycle of a challenge submission: idle → loading → success/error
sealed class CreateUiState {
    object Idle : CreateUiState()
    object Loading : CreateUiState()
    object Success : CreateUiState()
    data class Error(val message: String) : CreateUiState()
}
