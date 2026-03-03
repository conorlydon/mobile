package com.example.mobile.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mobile.domain.challenges.Challenge
import kotlinx.datetime.LocalDate

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "team_name") val teamName: String,
    @ColumnInfo(name = "skill_level") val skillLevel: String,
    val location: String,
    val date: LocalDate,
    @ColumnInfo(name = "created_by_email") val createdByEmail: String?
)

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
