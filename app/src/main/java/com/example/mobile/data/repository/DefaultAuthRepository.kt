package com.example.mobile.data.repository

import com.example.mobile.SupabaseClient

class DefaultAuthRepository : AuthRepository {
    override fun hasActiveSession(): Boolean = SupabaseClient.hasActiveSession()

    override suspend fun login(email: String, password: String) {
        SupabaseClient.signInWithEmail(email = email, password = password)
    }

    override suspend fun register(
        email: String,
        password: String,
        skillLevel: String,
        eircode: String
    ) {
        SupabaseClient.registerTeam(
            email = email,
            password = password,
            skillLevel = skillLevel,
            eircode = eircode
        )
    }

    override suspend fun logout() {
        SupabaseClient.signOut()
    }
}
