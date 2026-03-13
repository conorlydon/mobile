package com.example.mobile.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobile.data.repository.AuthRepository
import com.example.mobile.data.repository.DefaultAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginUiState: StateFlow<AuthUiState> = _loginUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val registerUiState: StateFlow<AuthUiState> = _registerUiState.asStateFlow()

    private val _logoutUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val logoutUiState: StateFlow<AuthUiState> = _logoutUiState.asStateFlow()

    fun hasActiveSession(): Boolean = repository.hasActiveSession()

    fun login(email: String, password: String) {
        _loginUiState.value = AuthUiState.Loading
        viewModelScope.launch {
            runCatching {
                repository.login(email = email.trim(), password = password)
            }.onSuccess {
                _loginUiState.value = AuthUiState.Success
            }.onFailure { error ->
                Log.e(TAG, "login failed", error)
                _loginUiState.value = AuthUiState.Error(loginErrorMessage(email, password, error))
            }
        }
    }

    fun register(
        email: String,
        password: String,
        teamName: String,
        location: String
    ) {
        _registerUiState.value = AuthUiState.Loading
        viewModelScope.launch {
            runCatching {
                repository.register(
                    email = email.trim(),
                    password = password,
                    teamName = teamName.trim(),
                    location = location.trim()
                )
            }.onSuccess {
                _registerUiState.value = AuthUiState.Success
            }.onFailure { error ->
                Log.e(TAG, "register failed", error)
                _registerUiState.value = AuthUiState.Error(
                    registerErrorMessage(email, password, teamName, location, error)
                )
            }
        }
    }

    fun logout() {
        _logoutUiState.value = AuthUiState.Loading
        viewModelScope.launch {
            runCatching { repository.logout() }
                .onSuccess { _logoutUiState.value = AuthUiState.Success }
                .onFailure { error ->
                    Log.e(TAG, "logout failed", error)
                    _logoutUiState.value = AuthUiState.Error(
                        error.message ?: "Couldn't log out. Please try again."
                    )
                }
        }
    }

    fun resetLoginState() {
        _loginUiState.value = AuthUiState.Idle
    }

    fun resetRegisterState() {
        _registerUiState.value = AuthUiState.Idle
    }

    fun resetLogoutState() {
        _logoutUiState.value = AuthUiState.Idle
    }

    companion object {
        private const val TAG = "AuthViewModel"
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AuthViewModel(DefaultAuthRepository())
            }
        }
    }
}

private fun loginErrorMessage(email: String, password: String, error: Throwable): String = when {
    email.isBlank() -> "Please enter your email address"
    password.isBlank() -> "Please enter your password"
    !email.contains("@") -> "Please enter a valid email address"
    password.length < 6 -> "Password must be at least 6 characters long"
    error.message?.contains("Invalid login", ignoreCase = true) == true ->
        "Incorrect email or password. Please try again."
    error.message?.contains("User not found", ignoreCase = true) == true ->
        "No account found with this email. Please register first."
    else -> "Login failed. Please check your credentials and try again."
}

private fun registerErrorMessage(
    email: String,
    password: String,
    teamName: String,
    location: String,
    error: Throwable
): String = when {
    teamName.isBlank() -> "Please enter your team name"
    location.isBlank() -> "Please enter your location"
    email.isBlank() -> "Please enter your email address"
    password.isBlank() -> "Please enter a password"
    !email.contains("@") -> "Please enter a valid email address"
    password.length < 6 -> "Password must be at least 6 characters long"
    error.message?.contains("already registered", ignoreCase = true) == true ->
        "An account with this email already exists. Please login instead."
    error.message?.contains("weak password", ignoreCase = true) == true ->
        "Password is too weak. Please choose a stronger password."
    else -> "Registration failed. Please check your information and try again."
}
