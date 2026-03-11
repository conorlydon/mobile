package com.example.mobile.presentation.challenges

import com.example.mobile.domain.challenges.Challenge

// Models the three states a detail screen can be in while loading from the local DB
sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val challenge: Challenge) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}
