package com.turboswitcher

import android.app.Application
import androidx.room.Room
import androidx.work.*
import com.turboswitcher.data.local.AppDatabase
import com.turboswitcher.domain.learning.SummaryWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

class TurboSwitcherApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "turboswitcher.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("TurboSwitcher Application initialized")
        scheduleSummaryWork()
    }

    private fun scheduleSummaryWork() {
        try {
            val request = PeriodicWorkRequestBuilder<SummaryWorker>(
                1,
                TimeUnit.DAYS
            ).build()
            
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "summary_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Timber.d("Summary work scheduled successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule summary work")
        }
    }
}
