package com.example.mobile.presentation.challenges

import com.example.mobile.domain.challenges.Challenge

// Represents every possible state the dashboard screen can be in
// The viewmodel sets the state based on what happens
sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val challenges: List<Challenge>) : DashboardUiState()
    object Empty : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
} // The compiler knows these are the only states the dashboardui can be in
