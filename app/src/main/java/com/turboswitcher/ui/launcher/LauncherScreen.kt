package com.turboswitcher.ui.launcher

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turboswitcher.TurboSwitcherApplication
import com.turboswitcher.data.local.PinnedContextEntity
import com.turboswitcher.data.repository.LauncherStateRepository
import com.turboswitcher.domain.launcher.LauncherStateManager
import com.turboswitcher.domain.launcher.ResumeRanker
import timber.log.Timber

@Composable
fun LauncherScreen(stateManager: LauncherStateManager) {
    val context = LocalContext.current
    val stateRepository = remember {
        LauncherStateRepository(
            (context.applicationContext as TurboSwitcherApplication).database.pinnedContextDao()
        )
    }
    val resumeRanker = remember { ResumeRanker(stateRepository) }

    // Use collectAsStateWithLifecycle for proper reactive updates
    val pinnedList by stateRepository.observePinned()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    
    var resumeCandidate by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pinnedList) {
        try {
            resumeCandidate = resumeRanker.getBestResumeCandidate()
            Timber.d("Resume candidate updated: $resumeCandidate")
        } catch (e: Exception) {
            Timber.e(e, "Failed to get resume candidate")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TurboSwitcher", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { stateManager.toggleExpanded() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))

                // Resume last
                if (resumeCandidate != null) {
                    Button(
                        onClick = {
                            resumeCandidate?.let { pkg ->
                                try {
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                                    launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    if (launchIntent != null) {
                                        context.startActivity(launchIntent)
                                        Timber.d("Launched app: $pkg")
                                    }
                                    stateManager.toggleExpanded()
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to launch app: $pkg")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Resume last")
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            item {
                // Pinned row
                Text("Pinned", style = MaterialTheme.typography.titleMedium)
                if (pinnedList.isNotEmpty()) {
                    PinnedRow(pinnedList) { pkg ->
                        try {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                            launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                                Timber.d("Launched app: $pkg")
                            }
                            stateManager.toggleExpanded()
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to launch app: $pkg")
                        }
                    }
                } else {
                    Text("No pinned apps yet", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(8.dp))
            }

            item {
                // Recent row
                Text("Recent", style = MaterialTheme.typography.titleMedium)
                RecentRow(recentApps = emptyList(), onItemClick = {})

                Spacer(Modifier.height(8.dp))
            }

            item {
                // Search bar
                SearchBar {
                    stateManager.setScreen(LauncherStateManager.LauncherScreen.SEARCH)
                }

                Spacer(Modifier.height(8.dp))
            }

            item {
                // Workflows row
                Text("Workflows", style = MaterialTheme.typography.titleMedium)
                WorkflowRow(workflows = emptyList(), onWorkflowClick = {})
            }
        }
    }
}
