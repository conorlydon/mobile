package com.example.mobile.presentation.auth
// Note a class of a parent interface can inherit multiple interfaces at a time
sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
} // Same job as the sealed class
