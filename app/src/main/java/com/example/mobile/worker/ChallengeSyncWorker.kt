package com.example.mobile.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mobile.data.local.AppDatabase
import com.example.mobile.data.repository.DefaultChallengeRepository

class ChallengeSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Challenge sync started (attempt ${runAttemptCount + 1})")
        return try {
            val database = AppDatabase.getInstance(applicationContext)
            val repository = DefaultChallengeRepository(database, database.challengeDao())
            repository.refreshChallenges()
            Log.d(TAG, "Challenge sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Challenge sync failed, will retry: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "challenge_sync"
        private const val TAG = "ChallengeSyncWorker"
    }
}
