package com.example.mobile.domain.challenges

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable // class can be converted to/from JSON.
data class Challenge(
    val id: String,
    @SerialName("team_name") val teamName: String,
    @SerialName("skill_level") val skillLevel: String,
    val location: String,
    val date: LocalDate,
    @SerialName("created_by_email") val createdByEmail: String? = null
    // Set server side not client side
)

fun LocalDate.display(): String {
    val mon = month.name[0] + month.name.drop(1).take(2).lowercase()
    return "$day $mon $year"
} // extension function to format dates
