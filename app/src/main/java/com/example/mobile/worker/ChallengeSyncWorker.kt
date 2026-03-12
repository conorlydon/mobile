package com.example.mobile.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mobile.data.local.AppDatabase
import com.example.mobile.data.repository.DefaultChallengeRepository

class ChallengeSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getInstance(applicationContext)
            val repository = DefaultChallengeRepository(database, database.challengeDao())
            repository.refreshChallenges()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "challenge_sync"
    }
}
