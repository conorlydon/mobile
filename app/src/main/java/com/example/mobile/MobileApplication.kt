package com.example.mobile

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mobile.worker.ChallengeSyncWorker
import java.util.concurrent.TimeUnit

class MobileApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ChallengeSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ChallengeSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                .build()
        )
        Log.d(TAG, "Challenge sync WorkManager enqueued")
    }

    companion object {
        private const val TAG = "MobileApplication"
    }
}
