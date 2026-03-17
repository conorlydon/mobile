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

// Concrete implementation of ChallengeRepository.
// Repository pattern: the ViewModel calls this via the ChallengeRepository interface —
// it doesn't know or care whether data comes from Room or Supabase.
// Read source = Room (local, fast, offline-capable, reactive Flow).
// Write target = Supabase first, then Room is updated immediately after.
class DefaultChallengeRepository(
    private val database: AppDatabase,
    private val challengeDao: ChallengeDao
) : ChallengeRepository {

    // Reads always come from Room — returns a Flow so the UI updates automatically when data changes.
    // Maps ChallengeEntity (persistence model) to Challenge (domain model) before returning.
    override fun observeActiveChallenges(): Flow<List<Challenge>> {
        return challengeDao.observeActiveChallenges(today = today())
            .map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeChallenge(challengeId: String): Flow<Challenge?> {
        return challengeDao.observeChallengeById(challengeId).map { row -> row?.toDomain() }
    }

    // Syncs challenges from Supabase into Room atomically.
    // withTransaction ensures clearAll + upsertAll are treated as one operation —
    // if upsertAll fails, Room rolls back to the previous state. The UI never sees an empty or partial list.
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
            id = UUID.randomUUID().toString(), // client-side UUID so Room can be updated immediately without waiting for DB
            teamName = teamName,
            skillLevel = skillLevel,
            location = location,
            date = date,
            createdByEmail = null
        )

        SupabaseClient.insertChallenge(challenge) // write to remote first
        challengeDao.upsert(challenge.toEntity()) // then update local Room cache immediately
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
