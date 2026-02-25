package com.example.mobile

object MetricsService {
    suspend fun track(eventName: String) {
        try {
            SupabaseClient.postgrest["events"].insert(
                mapOf("event_name" to eventName)
            )
        } catch (_: Exception) {
            // Silently ignore — metrics must not disrupt app flow
        }
    }
}
