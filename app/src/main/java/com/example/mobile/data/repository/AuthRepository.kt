package com.example.mobile.data.repository

interface AuthRepository {
    fun hasActiveSession(): Boolean
    suspend fun login(email: String, password: String)
    suspend fun register(
        email: String,
        password: String,
        teamName: String,
        location: String
    )
    suspend fun logout()
}
