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

    // onCreate() runs once per process lifetime, before any Activity or screen is shown.
    // This is the correct place to schedule global services like WorkManager — unlike MainActivity,
    // this is never recreated on device rotation or navigation, so there's no risk of duplicate scheduling.
    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ChallengeSyncWorker.WORK_NAME,           // the unique name "challenge_sync" identifies this job
            ExistingPeriodicWorkPolicy.KEEP,         // if a job already exists, leave it alone — don't reset its timer
            PeriodicWorkRequestBuilder<ChallengeSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                .build()
        )
        Log.d(TAG, "Challenge sync WorkManager enqueued")
    }

    // companion object is Kotlin's equivalent of Java static members.
    // TAG is defined here so it's shared across all instances of MobileApplication
    // and can be used in Log calls without needing an instance.
    companion object {
        private const val TAG = "MobileApplication"
    }
}
