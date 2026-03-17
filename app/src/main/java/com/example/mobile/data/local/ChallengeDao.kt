package com.example.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

// Data Access Object — defines all SQL operations on the challenges table.
// Room generates the implementation at compile time from these annotations.
@Dao
interface ChallengeDao {
    // Returns a Flow — when the database changes, a new list is emitted automatically.
    // This means the UI updates without any manual refresh.
    // Only returns challenges where date >= today, ordered soonest first.
    @Query("SELECT * FROM challenges WHERE date >= :today ORDER BY date ASC")
    fun observeActiveChallenges(today: LocalDate): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE id = :challengeId")
    fun observeChallengeById(challengeId: String): Flow<ChallengeEntity?>

    // OnConflictStrategy.REPLACE = if a row with the same primary key exists, replace it.
    // This gives upsert behaviour: works for both inserts and updates in a single call.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(challenge: ChallengeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(challenges: List<ChallengeEntity>)

    // Deletes all rows — used in refreshChallenges() inside a transaction before re-inserting.
    @Query("DELETE FROM challenges")
    suspend fun clearAll()
}
