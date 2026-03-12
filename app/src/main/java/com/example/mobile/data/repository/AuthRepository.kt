package com.example.mobile.data.repository

interface AuthRepository {
    fun hasActiveSession(): Boolean
    suspend fun login(email: String, password: String)
    suspend fun register(
        email: String,
        password: String,
        skillLevel: String,
        eircode: String
    )
    suspend fun logout()
}
