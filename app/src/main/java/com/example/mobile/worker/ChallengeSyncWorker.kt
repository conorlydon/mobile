package com.example.mobile.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mobile.data.local.AppDatabase
import com.example.mobile.data.repository.DefaultChallengeRepository

// CoroutineWorker is used instead of plain Worker because refreshChallenges() is a suspend function.
// CoroutineWorker's doWork() is also suspend, so it can call suspend functions directly without blocking a thread.
// WorkManager guarantees this work runs even if the app is closed or the device restarts.
class ChallengeSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // doWork() is called by WorkManager every 15 minutes (when network is available).
    override suspend fun doWork(): Result {
        // runAttemptCount is provided by WorkManager — tracks how many times this job has been attempted.
        // Starts at 0, so +1 gives a human-readable attempt number in the log (e.g. "attempt 1", "attempt 2").
        Log.d(TAG, "Challenge sync started (attempt ${runAttemptCount + 1})")
        return try {
            val database = AppDatabase.getInstance(applicationContext)
            val repository = DefaultChallengeRepository(database, database.challengeDao())
            repository.refreshChallenges() // fetches from Supabase and updates Room atomically
            Log.d(TAG, "Challenge sync completed successfully")
            Result.success() // tells WorkManager the job completed — schedule next run
        } catch (e: Exception) {
            Log.w(TAG, "Challenge sync failed, will retry: ${e.message}")
            Result.retry() // tells WorkManager to retry with exponential backoff — each retry waits progressively longer
        }
    }

    // companion object holds constants shared across the app without needing an instance.
    // WORK_NAME is used in MobileApplication to identify this job — must match when enqueuing and referencing it.
    companion object {
        const val WORK_NAME = "challenge_sync"
        private const val TAG = "ChallengeSyncWorker"
    }
}
