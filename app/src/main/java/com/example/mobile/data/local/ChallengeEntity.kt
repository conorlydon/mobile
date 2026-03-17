package com.example.mobile.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mobile.domain.challenges.Challenge
import kotlinx.datetime.LocalDate

// @Entity maps this data class to a table in the Room (SQLite) database.
// @ColumnInfo maps camelCase Kotlin field names to snake_case SQL column names.
// This is the persistence model — separate from the domain model (Challenge).
@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "team_name") val teamName: String,
    @ColumnInfo(name = "skill_level") val skillLevel: String,
    val location: String,
    val date: LocalDate, // stored as a String via ChallengeTypeConverters — Room can't store LocalDate natively
    @ColumnInfo(name = "created_by_email") val createdByEmail: String?
)

// Mapper functions convert between the persistence model (ChallengeEntity) and the domain model (Challenge).
// The ViewModel and UI only ever see the domain model — they are unaware of the database structure.
fun ChallengeEntity.toDomain(): Challenge = Challenge(
    id = id,
    teamName = teamName,
    skillLevel = skillLevel,
    location = location,
    date = date,
    createdByEmail = createdByEmail
)

fun Challenge.toEntity(): ChallengeEntity = ChallengeEntity(
    id = id,
    teamName = teamName,
    skillLevel = skillLevel,
    location = location,
    date = date,
    createdByEmail = createdByEmail
)
