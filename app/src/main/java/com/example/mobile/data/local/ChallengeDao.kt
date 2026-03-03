package com.example.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges WHERE date >= :today ORDER BY date ASC")
    fun observeActiveChallenges(today: LocalDate): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE id = :challengeId")
    fun observeChallengeById(challengeId: String): Flow<ChallengeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(challenge: ChallengeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(challenges: List<ChallengeEntity>)

    @Query("DELETE FROM challenges")
    suspend fun clearAll()
}
