package com.turboswitcher.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.turboswitcher.R
import com.turboswitcher.TurboSwitcherApplication
import com.turboswitcher.data.repository.LauncherStateRepository
import com.turboswitcher.domain.launcher.LauncherStateManager
import com.turboswitcher.ui.launcher.BubbleLauncher
import com.turboswitcher.ui.launcher.LauncherScreen
import com.turboswitcher.ui.search.SearchScreen
import com.turboswitcher.ui.theme.TurboSwitcherTheme
import kotlinx.coroutines.launch
import timber.log.Timber

class OverlayLauncherService : LifecycleService() {

    private lateinit var windowManager: WindowManager
    private lateinit var bubbleView: ComposeView
    private var expandedView: ComposeView? = null
    private lateinit var stateManager: LauncherStateManager

    override fun onCreate() {
        super.onCreate()
        Timber.d("OverlayLauncherService created")
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val db = (application as TurboSwitcherApplication).database
        val stateRepository = LauncherStateRepository(db.pinnedContextDao())
        stateManager = LauncherStateManager(this, stateRepository)

        createNotificationChannel()
        startForeground(1, buildNotification())
        createBubble()

        lifecycleScope.launch {
            stateManager.expanded.collect { expanded ->
                if (expanded) showExpanded() else dismissExpanded()
            }
        }
        
        lifecycleScope.launch {
            stateManager.screen.collect { screen ->
                // Update the expanded view based on screen state
                if (stateManager.expanded.value) {
                    dismissExpanded()
                    showExpanded()
                }
            }
        }
    }

    private fun createBubble() {
        try {
            bubbleView = ComposeView(this).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent {
                    TurboSwitcherTheme {
                        BubbleLauncher(stateManager = stateManager)
                    }
                }
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.START
            params.x = 100
            params.y = 200
            windowManager.addView(bubbleView, params)
            Timber.d("Bubble view created and added")
        } catch (e: Exception) {
            Timber.e(e, "Failed to create bubble")
        }
    }

    private fun showExpanded() {
        if (expandedView != null) return
        try {
            expandedView = ComposeView(this).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent {
                    TurboSwitcherTheme {
                        when (stateManager.screen.value) {
                            LauncherStateManager.LauncherScreen.SEARCH -> {
                                SearchScreen(stateManager) {
                                    stateManager.setScreen(LauncherStateManager.LauncherScreen.HOME)
                                }
                            }
                            else -> LauncherScreen(stateManager = stateManager)
                        }
                    }
                }
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER
            windowManager.addView(expandedView, params)
            Timber.d("Expanded view shown")
        } catch (e: Exception) {
            Timber.e(e, "Failed to show expanded view")
        }
    }

    private fun dismissExpanded() {
        try {
            expandedView?.let { windowManager.removeView(it) }
            expandedView = null
            Timber.d("Expanded view dismissed")
        } catch (e: Exception) {
            Timber.e(e, "Failed to dismiss expanded view")
        }
    }

    override fun onDestroy() {
        try {
            if (::bubbleView.isInitialized) windowManager.removeView(bubbleView)
            dismissExpanded()
            Timber.d("OverlayLauncherService destroyed")
        } catch (e: Exception) {
            Timber.e(e, "Error during service destruction")
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "turboswitcher_overlay",
                "TurboSwitcher Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "turboswitcher_overlay")
            .setContentTitle("TurboSwitcher")
            .setContentText("Floating launcher active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
