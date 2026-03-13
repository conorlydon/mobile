package com.example.mobile.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.mobile.SupabaseClient
import com.example.mobile.data.local.AppDatabase
import com.example.mobile.data.local.ChallengeDao
import com.example.mobile.data.local.toDomain
import com.example.mobile.data.local.toEntity
import com.example.mobile.domain.challenges.Challenge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import java.util.UUID

class DefaultChallengeRepository(
    private val database: AppDatabase,
    private val challengeDao: ChallengeDao
) : ChallengeRepository {

    override fun observeActiveChallenges(): Flow<List<Challenge>> {
        return challengeDao.observeActiveChallenges(today = today())
            .map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeChallenge(challengeId: String): Flow<Challenge?> {
        return challengeDao.observeChallengeById(challengeId).map { row -> row?.toDomain() }
    }

    override suspend fun refreshChallenges() {
        val remote = SupabaseClient.fetchChallenges()
        Log.d(TAG, "Fetched ${remote.size} challenges from Supabase")
        database.withTransaction {
            challengeDao.clearAll()
            challengeDao.upsertAll(remote.map { it.toEntity() })
        }
    }

    override suspend fun requestJoinChallenge(challenge: Challenge) {
        SupabaseClient.requestJoinChallenge(challenge)
    }

    override suspend fun createChallenge(
        skillLevel: String,
        date: LocalDate
    ) {
        val (teamName, location) = SupabaseClient.getCurrentUserProfile()
        val challenge = Challenge(
            id = UUID.randomUUID().toString(),
            teamName = teamName,
            skillLevel = skillLevel,
            location = location,
            date = date,
            createdByEmail = null
        )

        SupabaseClient.insertChallenge(challenge)
        challengeDao.upsert(challenge.toEntity())
    }
    companion object {
        private const val TAG = "DefaultChallengeRepository"
    }
}

private fun today(): LocalDate {
    val now = java.util.Calendar.getInstance()
    val year = now.get(java.util.Calendar.YEAR)
    val month = now.get(java.util.Calendar.MONTH) + 1
    val day = now.get(java.util.Calendar.DAY_OF_MONTH)
    return LocalDate(year, month, day)
}
