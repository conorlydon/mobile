package com.example.mobile

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import com.example.mobile.domain.challenges.Challenge
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object SupabaseClient {
    // Replace with your Supabase project URL and anon key
    private val supabase= createSupabaseClient(
        supabaseUrl = "https://xzmltjlyybkuuidcaexi.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh6bWx0amx5eWJrdXVpZGNhZXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzA4OTYwODQsImV4cCI6MjA4NjQ3MjA4NH0.pDA8xTfRvsu6LLWo9O6iDlIPdnEqAd-w1YfJVatzgQQ"
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
        skillLevel: String,
        eircode: String
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
                put("skill_level", skillLevel)
                put("eircode", eircode)
            }
        )
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

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    fun hasActiveSession(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }
}
