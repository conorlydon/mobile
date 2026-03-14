package com.example.mobile.presentation.challenges

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobile.data.local.AppDatabase
import com.example.mobile.data.repository.ChallengeRepository
import com.example.mobile.data.repository.DefaultChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import com.example.mobile.domain.challenges.Challenge

class ChallengesViewModel(
    private val repository: ChallengeRepository
) : ViewModel() {

    // true initially so the UI shows a spinner before the first refresh completes
    private val _isRefreshing = MutableStateFlow(true)
    private val _dashboardError = MutableStateFlow<String?>(null)

    // combine merges three flows into one — re-emits whenever any of them changes
    val dashboardUiState: StateFlow<DashboardUiState> = combine(
        repository.observeActiveChallenges(),
        _isRefreshing,
        _dashboardError
    ) { challenges, isRefreshing, error ->
        when {
            challenges.isNotEmpty() -> DashboardUiState.Success(challenges)
            isRefreshing -> DashboardUiState.Loading
            error != null -> DashboardUiState.Error(error)
            else -> DashboardUiState.Empty
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState.Loading)

    private val _createUiState = MutableStateFlow<CreateUiState>(CreateUiState.Idle)
    private val _joinChallengeUiState = MutableStateFlow<JoinChallengeUiState>(JoinChallengeUiState.Idle)
    val joinChallengeUiState: StateFlow<JoinChallengeUiState> = _joinChallengeUiState.asStateFlow()
    val createUiState: StateFlow<CreateUiState> = _createUiState.asStateFlow()

    init {
        refreshChallenges()
    }

    // Maps the nullable Flow from the repo into a typed DetailUiState
    fun observeChallenge(challengeId: String): Flow<DetailUiState> {
        return repository.observeChallenge(challengeId).map { challenge ->
            if (challenge != null) DetailUiState.Success(challenge)
            else DetailUiState.Error("Challenge not found")
        }
    }

    fun refreshChallenges() {
        _isRefreshing.value = true
        _dashboardError.value = null
        viewModelScope.launch {
            // runCatching is a clean alternative to try/catch for coroutine operations
            runCatching { repository.refreshChallenges() }
                .onFailure {
                    Log.e(TAG, "refreshChallenges failed", it)
                    _dashboardError.value = when {
                        it.message?.contains("network", ignoreCase = true) == true ->
                            "Unable to connect. Please check your internet connection and try again."
                        it.message?.contains("timeout", ignoreCase = true) == true ->
                            "Connection timed out. Please try again."
                        it.message?.contains("unauthorized", ignoreCase = true) == true ->
                            "Please login to view challenges."
                        else -> "Couldn't load challenges. Please pull down to refresh."
                    }
                }
            _isRefreshing.value = false
        }
    }

    fun createChallenge(
        skillLevel: String,
        date: LocalDate
    ) {
        _createUiState.value = CreateUiState.Loading
        viewModelScope.launch {
            runCatching {
                repository.createChallenge(
                    skillLevel = skillLevel.trim(),
                    date = date
                )
            }.onSuccess {
                _createUiState.value = CreateUiState.Success
            }.onFailure {
                Log.e(TAG, "createChallenge failed", it)
                _createUiState.value = CreateUiState.Error(
                    when {
                        skillLevel.isBlank() -> "Please select a skill level"
                        it.message?.contains("network", ignoreCase = true) == true ->
                            "Unable to connect. Please check your internet connection and try again."
                        else -> "Couldn't create challenge. Please try again."
                    }
                )
            }
        }
    }

    fun requestJoinChallenge(challenge: Challenge) {
        // Immediately show a loading spinner while the request is in flight
        _joinChallengeUiState.value = JoinChallengeUiState.Loading

        // Scope so the coroutine is cancelled if the ViewModel is destroyed
        viewModelScope.launch {
            runCatching {
                repository.requestJoinChallenge(challenge)
            }.onSuccess {
                _joinChallengeUiState.value = JoinChallengeUiState.Success(
                    "Join request sent to ${challenge.teamName}."
                )
            }.onFailure {
                Log.e(TAG, "requestJoinChallenge failed", it)
                _joinChallengeUiState.value = JoinChallengeUiState.Error(
                    when {
                        it.message?.contains("already", ignoreCase = true) == true ->
                            "You already requested to join this challenge."
                        it.message?.contains("auth", ignoreCase = true) == true ->
                            "Please login to request this challenge."
                        it.message?.contains("owner", ignoreCase = true) == true ->
                            "You can't join your own challenge."
                        it.message?.contains("contact", ignoreCase = true) == true ->
                            "This challenge has no contact email yet."
                        it.message?.contains("network", ignoreCase = true) == true ->
                            "Unable to connect. Please check your internet and try again."
                        else -> "Couldn't send your join request. Please try again."
                    }
                )
            }
        }
    }

    fun resetJoinChallengeState() {
        _joinChallengeUiState.value = JoinChallengeUiState.Idle
    }

    // Called after navigating away from create screen so state doesn't persist on re-entry
    fun resetCreateState() {
        _createUiState.value = CreateUiState.Idle
    }

    // Factory manually wires dependencies since we're not using a DI framework
    companion object {
        private const val TAG = "ChallengesViewModel"
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val database = AppDatabase.getInstance(application)
                val repository = DefaultChallengeRepository(
                    database = database,
                    challengeDao = database.challengeDao()
                )
                ChallengesViewModel(repository)
            }
        }
    }
}
