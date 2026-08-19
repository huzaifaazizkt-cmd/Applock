package com.example.applock.Design.screens

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.launch

@Composable
fun AppListScreen(context: Context) {

    val pm = context.packageManager
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreManager(context)

    var selectedTab by remember {
        mutableStateOf(0)
    }

    val apps = remember {
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter {
                it.packageName != context.packageName
            }
    }

    val lockedApps by dataStore.lockedAppsFlow.collectAsState(
        initial = emptySet()
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // =================================================
        // TITLE
        // =================================================

        Text(
            text = "App Lock",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        // =================================================
        // TABS
        // =================================================

        TabRow(
            selectedTabIndex = selectedTab
        ) {

            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                },
                text = {
                    Text("Unlocked")
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null
                    )
                }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                },
                text = {
                    Text("Locked")
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    )
                }
            )
        }

        // =================================================
        // FILTER APPS
        // =================================================

        val filteredApps =
            when (selectedTab) {

                0 -> apps.filter {
                    !lockedApps.contains(it.packageName)
                }

                else -> apps.filter {
                    lockedApps.contains(it.packageName)
                }
            }

        // =================================================
        // APP LIST
        // =================================================

        LazyColumn {

            items(
                items = filteredApps,
                key = {
                    it.packageName
                }
            ) { app ->

                val appName =
                    pm.getApplicationLabel(app).toString()

                val icon =
                    pm.getApplicationIcon(app)

                val isLocked =
                    lockedApps.contains(
                        app.packageName
                    )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    // =================================================
                    // APP ICON
                    // NOT CLICKABLE
                    // =================================================

                    Image(
                        bitmap = icon
                            .toBitmap()
                            .asImageBitmap(),

                        contentDescription = null,

                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    // =================================================
                    // APP NAME
                    // NOT CLICKABLE
                    // =================================================

                    Text(
                        text = appName,
                        modifier = Modifier.weight(1f)
                    )

                    // =================================================
                    // LOCK / UNLOCK BUTTON
                    // ONLY THIS IS CLICKABLE
                    // =================================================

                    IconButton(
                        onClick = {

                            scope.launch {

                                if (isLocked) {

                                    // 🔓 UNLOCK APP

                                    dataStore
                                        .removeLockedApp(
                                            app.packageName
                                        )

                                } else {

                                    // 🔒 LOCK APP

                                    dataStore
                                        .saveLockedApp(
                                            app.packageName
                                        )
                                }
                            }
                        }
                    ) {

                        Icon(
                            imageVector =
                                if (isLocked) {
                                    Icons.Default.Lock
                                } else {
                                    Icons.Default.LockOpen
                                },

                            contentDescription =
                                if (isLocked) {
                                    "Unlock $appName"
                                } else {
                                    "Lock $appName"
                                }
                        )
                    }
                }

                Divider()
            }
        }
    }
}