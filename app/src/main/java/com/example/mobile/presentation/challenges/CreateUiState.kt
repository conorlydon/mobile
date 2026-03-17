package com.example.mobile.presentation.challenges

// Tracks the lifecycle of a challenge submission: idle → loading → success/error
// Note a sub class of a parent sealed class can only inherit one sealed class at a time
sealed class CreateUiState {
    object Idle : CreateUiState()
    object Loading : CreateUiState()
    object Success : CreateUiState()
    data class Error(val message: String) : CreateUiState()
}
