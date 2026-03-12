package com.example.mobile.data.repository

import com.example.mobile.domain.challenges.Challenge
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ChallengeRepository {
    fun observeActiveChallenges(): Flow<List<Challenge>>
    fun observeChallenge(challengeId: String): Flow<Challenge?>
    suspend fun refreshChallenges()
    suspend fun requestJoinChallenge(challenge: Challenge)
    suspend fun createChallenge(
        teamName: String,
        skillLevel: String,
        location: String,
        date: LocalDate
    )
}
