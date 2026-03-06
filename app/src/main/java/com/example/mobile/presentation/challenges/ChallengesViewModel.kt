package com.example.mobile.presentation.challenges

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobile.data.local.AppDatabase
import com.example.mobile.data.repository.ChallengeRepository
import com.example.mobile.data.repository.DefaultChallengeRepository
import com.example.mobile.domain.challenges.Challenge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class ChallengesViewModel(
    private val repository: ChallengeRepository
) : ViewModel() {

    val activeChallenges: StateFlow<List<Challenge>> = repository.observeActiveChallenges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refreshChallenges()
    }

    fun observeChallenge(challengeId: String): Flow<Challenge?> {
        return repository.observeChallenge(challengeId)
    }

    fun refreshChallenges() {
        viewModelScope.launch {
            runCatching { repository.refreshChallenges() }
                .onFailure { 
                    _errorMessage.value = when {
                        it.message?.contains("network", ignoreCase = true) == true -> "Unable to connect. Please check your internet connection and try again."
                        it.message?.contains("timeout", ignoreCase = true) == true -> "Connection timed out. Please try again."
                        it.message?.contains("unauthorized", ignoreCase = true) == true -> "Please login to view challenges."
                        else -> "Couldn't load challenges. Please pull down to refresh."
                    }
                }
        }
    }

    fun createChallenge(
        teamName: String,
        skillLevel: String,
        location: String,
        date: LocalDate,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                repository.createChallenge(
                    teamName = teamName.trim(),
                    skillLevel = skillLevel.trim(),
                    location = location.trim(),
                    date = date
                )
            }.onSuccess {
                onSuccess()
            }.onFailure {
                _errorMessage.value = when {
                    teamName.isBlank() -> "Please enter a team name"
                    skillLevel.isBlank() -> "Please enter a skill level"
                    location.isBlank() -> "Please enter a location"
                    it.message?.contains("network", ignoreCase = true) == true -> "Unable to connect. Please check your internet connection and try again."
                    it.message?.contains("exists", ignoreCase = true) == true -> "A challenge with these details already exists."
                    else -> "Couldn't create challenge. Please try again."
                }
            }
        }
    }

    companion object {
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
