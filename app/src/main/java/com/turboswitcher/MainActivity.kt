package com.turboswitcher

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.turboswitcher.service.OverlayLauncherService
import com.turboswitcher.ui.theme.TurboSwitcherTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { maybeStartOverlay() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        setContent {
            TurboSwitcherTheme {
                MainScreen(
                    onStartOverlay = { maybeStartOverlay() },
                    onGrantUsage = { requestUsagePermission() }
                )
            }
        }
    }

    private fun maybeStartOverlay() {
        if (Settings.canDrawOverlays(this)) {
            try {
                ContextCompat.startForegroundService(this, Intent(this, OverlayLauncherService::class.java))
                Toast.makeText(this, "Overlay started", Toast.LENGTH_SHORT).show()
                Timber.d("Overlay service started successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to start overlay service")
                Toast.makeText(this, "Failed to start overlay: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun requestUsagePermission() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        if (!hasUsagePermission()) {
            Toast.makeText(this, "Grant usage access to see recent apps", Toast.LENGTH_LONG).show()
        }
    }

    private fun hasUsagePermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}

@Composable
fun MainScreen(onStartOverlay: () -> Unit, onGrantUsage: () -> Unit) {
    var showPermissionInfo by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("TurboSwitcher", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Fast app launcher with smart suggestions",
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.height(24.dp))
        
        Button(onClick = onStartOverlay, modifier = Modifier.fillMaxWidth()) {
            Text("Start Overlay")
        }
        Spacer(Modifier.height(8.dp))
        
        Button(onClick = onGrantUsage, modifier = Modifier.fillMaxWidth()) {
            Text("Grant Usage Access")
        }
        
        Spacer(Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Permissions Info",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clickable { showPermissionInfo = !showPermissionInfo }
                    )
                }
                
                if (showPermissionInfo) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Overlay: Shows the floating bubble on your screen.\n" +
                        "Usage Access: Learns which apps you use most.\n" +
                        "Query Packages: Finds all installed apps.\n\n" +
                        "All data is stored locally on your device.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
