package com.turboswitcher.domain.learning

import android.content.Context
import android.content.pm.PackageManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.turboswitcher.data.repository.EventRepository
import com.turboswitcher.data.repository.LauncherStateRepository
import com.turboswitcher.data.local.PinnedContextEntity
import com.turboswitcher.TurboSwitcherApplication
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("SummaryWorker starting...")
            
            val db = (applicationContext as TurboSwitcherApplication).database
            val stateRepository = LauncherStateRepository(db.pinnedContextDao())
            val eventRepository = EventRepository(db.eventDao())
            val packageManager = applicationContext.packageManager

            // Promote frequently used apps to pinned contexts
            val eventCount = db.eventDao().countAppOpens()
            if (eventCount > 5) {
                val sevenDaysMs: Long = 7L * 24L * 3600L * 1000L
                val recentEvents = db.eventDao().observeEvents(System.currentTimeMillis() - sevenDaysMs).first()
                val appCounts = recentEvents
                    .filter { it.actionType == "APP_OPEN" && it.appPackage != null }
                    .groupingBy { it.appPackage!! }
                    .eachCount()
                val topApps = appCounts.entries.sortedByDescending { it.value }.take(5)
                
                for ((pkg, count) in topApps) {
                    val existing = stateRepository.getByPackage(pkg)
                    if (existing == null && count >= 10) {
                        try {
                            val appLabel = try {
                                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                                packageManager.getApplicationLabel(appInfo).toString()
                            } catch (e: PackageManager.NameNotFoundException) {
                                pkg.substringAfterLast('.')
                            }
                            
                            stateRepository.add(
                                PinnedContextEntity(
                                    label = appLabel,
                                    packageName = pkg,
                                    lastUsed = System.currentTimeMillis(),
                                    openCount = count,
                                    pinned = false
                                )
                            )
                            Timber.d("Promoted app to pinned: $pkg (label: $appLabel, count: $count)")
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to promote app: $pkg")
                        }
                    }
                }
            }

            // Clean old events (keep last 30 days)
            val thirtyDaysMs: Long = 30L * 24L * 3600L * 1000L
            val cutoff = System.currentTimeMillis() - thirtyDaysMs
            db.eventDao().deleteOldEvents(cutoff)
            Timber.d("Cleaned old events before: $cutoff")

            Timber.d("SummaryWorker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "SummaryWorker failed")
            Result.retry()
        }
    }
}
