package com.turboswitcher.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.turboswitcher.TurboSwitcherApplication
import com.turboswitcher.data.model.DeviceState
import com.turboswitcher.data.model.RawEvent
import com.turboswitcher.data.repository.EventRepository
import com.turboswitcher.domain.learning.EventCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber

class AccessibilityCaptureService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var eventCollector: EventCollector

    override fun onCreate() {
        super.onCreate()
        try {
            val db = (application as TurboSwitcherApplication).database
            val eventRepository = EventRepository(db.eventDao())
            eventCollector = EventCollector(eventRepository, scope)
            Timber.d("AccessibilityCaptureService initialized")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize AccessibilityCaptureService")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        try {
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    val packageName = event.packageName?.toString()
                    if (packageName != null && packageName.isNotEmpty()) {
                        eventCollector.capture(
                            RawEvent(
                                timestamp = System.currentTimeMillis(),
                                surfaceType = "window",
                                appPackage = packageName,
                                contextName = event.className?.toString(),
                                actionType = "APP_OPEN",
                                dwellTimeMs = 0,
                                openCount = 1,
                                returnCount = 0,
                                searchQuery = null,
                                resultClicked = null,
                                success = true,
                                deviceState = DeviceState(
                                    screenOn = true,
                                    charging = false,
                                    batteryLevelBand = 0,
                                    networkType = "unknown"
                                )
                            )
                        )
                        Timber.d("Captured app open event: $packageName")
                    }
                }
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    val packageName = event.packageName?.toString()
                    if (packageName != null && packageName.isNotEmpty()) {
                        Timber.d("Captured click event in: $packageName")
                    }
                }
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                    val packageName = event.packageName?.toString()
                    if (packageName != null && packageName.isNotEmpty()) {
                        Timber.d("Captured text change in: $packageName")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error processing accessibility event")
        }
    }

    override fun onInterrupt() {
        Timber.d("Accessibility service interrupted")
    }
    
    override fun onDestroy() {
        scope.cancel()
        Timber.d("AccessibilityCaptureService destroyed")
        super.onDestroy()
    }
}
