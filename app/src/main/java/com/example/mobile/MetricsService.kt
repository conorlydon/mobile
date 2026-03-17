package com.example.mobile

// COTC integration component — tracks user actions by inserting events into the Supabase `events` table.
// Kotlin object = singleton pattern. No instantiation needed — accessible anywhere in the app as MetricsService.track(...)
object MetricsService {
    // suspend fun — runs in a coroutine, does not block the main thread.
    // Events tracked: "login_viewed", "dashboard_viewed", "create_challenge_tapped", "club_joined"
    suspend fun track(eventName: String) {
        try {
            SupabaseClient.postgrest["events"].insert(
                mapOf("event_name" to eventName)
            )
        } catch (_: Exception) {
            // Silently ignore all exceptions — a failed analytics call must never crash or block the app.
            // Metrics are secondary to app function.
        }
    }
}
