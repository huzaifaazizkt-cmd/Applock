package com.example.applock.Design.screens



import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    // 📱 Installed Apps List
    val apps = remember {
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
    }

    // 🔒 Locked Apps State
    val lockedApps by dataStore.lockedAppsFlow.collectAsState(initial = emptySet())

    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp)
    ) {

        Text(
            text = "Select Apps to Lock",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {

            items(apps) { app ->

                val appName = pm.getApplicationLabel(app).toString()
                val icon = pm.getApplicationIcon(app)

                val isLocked = lockedApps.contains(app.packageName)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {

                            scope.launch {
                                if (isLocked) {
                                    dataStore.removeLockedApp(app.packageName)
                                } else {
                                    dataStore.saveLockedApp(app.packageName)
                                }
                            }

                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // 📱 App Icon
                    Image(
                        bitmap = icon.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(appName)
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // 🔘 Lock Status
                    Switch(
                        checked = isLocked,
                        onCheckedChange = {
                            scope.launch {
                                if (isLocked) {
                                    dataStore.removeLockedApp(app.packageName)
                                } else {
                                    dataStore.saveLockedApp(app.packageName)
                                }
                            }
                        }
                    )
                }

                Divider()
            }
        }
    }
}