package com.example.mobile

import com.example.mobile.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import com.example.mobile.domain.challenges.Challenge
import io.github.jan.supabase.supabaseJson
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object SupabaseClient {
    private val supabase= createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ){
        install(Auth)
        install(Postgrest)
    }

    val postgrest: Postgrest = supabase.postgrest

    suspend fun signUpWithEmail(email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun registerTeam(
        email: String,
        password: String,
        teamName: String,
        location: String
    ) {
        signUpWithEmail(email = email, password = password)

        val currentUser = supabase.auth.currentUserOrNull()
            ?: throw IllegalStateException(
                "No active session after sign up. For prototype environments using fake emails, " +
                        "disable Confirm email in Supabase Auth settings."
            )

        postgrest.from("users").upsert(
            buildJsonObject {
                put("id", currentUser.id)
                put("email", currentUser.email ?: email)
                put("team_name", teamName)
                put("location", location)
            }
        )
    }

    suspend fun getCurrentUserProfile(): Pair<String, String> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return Pair("", "")
        val rows = postgrest.from("users").select().decodeList<UserProfileData>()
        val profile = rows.firstOrNull { it.id == userId }
        return Pair(profile?.teamName ?: "", profile?.location ?: "")
    }

    suspend fun signInWithEmail(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun fetchChallenges(): List<Challenge> {
        return postgrest.from("challenges").select().decodeList()
    }

    suspend fun insertChallenge(challenge: Challenge) {
        val email = supabase.auth.currentUserOrNull()?.email ?: ""
        postgrest.from("challenges").insert(
            buildJsonObject {
                put("id", challenge.id)
                put("team_name", challenge.teamName)
                put("skill_level", challenge.skillLevel)
                put("location", challenge.location)
                put("date", challenge.date.toString())
                put("created_by_email", email)
            }
        )
    }

    suspend fun requestJoinChallenge(challenge: Challenge) {
        // Get the current user's email
        val requesterEmail = supabase.auth.currentUserOrNull()?.email
            ?: throw IllegalStateException("Authentication required to join a challenge")
        // Get the challenge owner's email
        val targetEmail = challenge.createdByEmail?.takeIf { it.isNotBlank() } // handles empty strings
            ?: throw IllegalStateException("Challenge owner contact is unavailable")
        // Can't join your own challenge
        if (targetEmail.equals(requesterEmail, ignoreCase = true)) {
            throw IllegalArgumentException("Challenge owner cannot join their own challenge")
        }
        // Duplicate check prevents spam requests to the same challenge
        // AI assisted - Used Claude to help implement a single request only rule to prevent spam
        val existing = postgrest.from("challenge_join_requests")
            .select {
                filter {
                    eq("challenge_id", challenge.id)
                    eq("requester_email", requesterEmail)
                } // eq means equals. So both conditions must be true. Like a SQL WHERE
            }
            .decodeList<JsonObject>() // converts raw supabase response to JSON object

        if (existing.isNotEmpty()) {
            throw IllegalStateException("You have already requested to join this challenge.")
        }

        // Client-generated UUID so we don't depend on DB auto-increment
        postgrest.from("challenge_join_requests").insert(
            buildJsonObject {
                put("id", UUID.randomUUID().toString())
                put("challenge_id", challenge.id)
                put("challenge_team_name", challenge.teamName)
                put("requester_email", requesterEmail)
                put("target_email", targetEmail)
                put("status", "pending")
            }
        )

        val (requesterTeamName, _) = getCurrentUserProfile() // _ is used since we don't care about the second value returned (location)

        // Email failure is non-fatal — the DB record is already saved so the request isn't lost if this fails
        runCatching {
            sendJoinRequestEmail(
                requesterEmail = requesterEmail,
                requesterTeamName = requesterTeamName,
                targetEmail = targetEmail,
                challengeTeamName = challenge.teamName
            )
        }.onFailure { Log.e("SupabaseClient", "Failed to send join request email", it) }
    }

    private suspend fun sendJoinRequestEmail(
        requesterEmail: String,
        requesterTeamName: String,
        targetEmail: String,
        challengeTeamName: String
    ) = withContext(Dispatchers.IO)
    // withContext changes the thread this runs on
      {
        Log.d("SupabaseClient", "Sending email to: $targetEmail from: $requesterEmail ($requesterTeamName) for challenge: $challengeTeamName")
        // Raw HttpURLConnection used to avoid adding an HTTP library dependency
        // AI assisted - Used Claude to help me to make a POST request in kotlin
        val connection = URL("https://api.emailjs.com/api/v1.0/email/send").openConnection() as HttpURLConnection // blocking call
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json") // so emailjs knows we are sending json
            connection.doOutput = true
            // Tells connection we intend to send a request body

            // Falls back to email if team name is blank (e.g. profile not fully set up)
            val displayName = if (requesterTeamName.isNotBlank()) requesterTeamName else requesterEmail
            val body = """
                {
                    "service_id": "service_k5qjmsw",
                    "template_id": "template_vcd7r8p",
                    "user_id": "${BuildConfig.EMAILJS_PUBLIC_KEY}",
                    "template_params": {
                        "to_email": "$targetEmail",
                        "name": "$displayName",
                        "message": "$displayName ($requesterEmail) has requested a friendly match against your team ($challengeTeamName). Reply to their email to arrange the details."
                    }
                }
            """.trimIndent()
            // Converts the JSON string to bytes to send the POST request
            //  .use{} ensures the stream is closed automatically after
            connection.outputStream.use { it.write(body.toByteArray()) }
            // Converting JSON string into raw bytes - network connections only understand bytes

            val code = connection.responseCode
            if (code !in 200..299) {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                throw IllegalStateException("Failed to send email (HTTP $code): $error")
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    fun hasActiveSession(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }

    @Serializable
    private data class UserProfileData(
        val id: String,
        @SerialName("team_name") val teamName: String = "",
        val location: String = ""
    )
}
