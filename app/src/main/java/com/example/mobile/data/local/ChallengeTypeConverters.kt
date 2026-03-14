package com.example.mobile.data.local
// AI Generated
import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

class ChallengeTypeConverters {
    @TypeConverter
    fun localDateToString(value: LocalDate): String = value.toString()

    @TypeConverter
    fun stringToLocalDate(value: String): LocalDate = LocalDate.parse(value)
}
