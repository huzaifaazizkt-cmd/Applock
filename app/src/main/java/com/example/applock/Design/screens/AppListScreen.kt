package com.example.applock.Design.screens

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.applock.R
import com.example.applock.data.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext


// =============================================================
// APP ITEM
// =============================================================

data class AppItem(
    val applicationInfo: ApplicationInfo,
    val appName: String,
    val searchName: String,
    val iconBitmap: androidx.compose.ui.graphics.ImageBitmap?
)


// =============================================================
// APP CACHE
// =============================================================

object AppListCache {

    @Volatile
    private var apps: List<AppItem>? = null

    private val preloadMutex = Mutex()


    // =========================================================
    // GET CACHE
    // =========================================================

    fun getApps(): List<AppItem>? {
        return apps
    }


    // =========================================================
    // PRELOAD APPS + ICONS
    // =========================================================

    suspend fun preload(
        context: Context
    ) {

        if (apps != null) {
            return
        }

        preloadMutex.withLock {

            if (apps != null) {
                return@withLock
            }

            try {

                val appContext =
                    context.applicationContext

                val result =
                    withContext(Dispatchers.IO) {

                        val pm =
                            appContext.packageManager


                        // =================================================
                        // ONLY LAUNCHER APPS
                        // =================================================

                        val launcherIntent =
                            Intent(
                                Intent.ACTION_MAIN
                            ).apply {

                                addCategory(
                                    Intent.CATEGORY_LAUNCHER
                                )
                            }


                        val launcherApps =
                            pm.queryIntentActivities(
                                launcherIntent,
                                PackageManager.MATCH_ALL
                            )


                        launcherApps
                            .mapNotNull { resolveInfo ->

                                try {

                                    val applicationInfo =
                                        resolveInfo
                                            .activityInfo
                                            ?.applicationInfo
                                            ?: return@mapNotNull null


                                    val packageName =
                                        applicationInfo.packageName


                                    // =================================================
                                    // REMOVE APPLOCK ITSELF
                                    // =================================================

                                    if (
                                        packageName ==
                                        appContext.packageName
                                    ) {

                                        return@mapNotNull null
                                    }


                                    // =================================================
                                    // APP NAME
                                    // =================================================

                                    val appName =
                                        resolveInfo
                                            .loadLabel(pm)
                                            .toString()


                                    // =================================================
                                    // APP ICON
                                    // =================================================

                                    val iconBitmap =
                                        try {

                                            pm.getApplicationIcon(
                                                applicationInfo
                                            )
                                                .toBitmap(
                                                    64,
                                                    64
                                                )
                                                .asImageBitmap()

                                        } catch (e: Exception) {

                                            null
                                        }


                                    AppItem(

                                        applicationInfo =
                                            applicationInfo,

                                        appName =
                                            appName,

                                        searchName =
                                            appName.lowercase(),

                                        iconBitmap =
                                            iconBitmap
                                    )

                                } catch (e: Exception) {

                                    null
                                }
                            }
                            .distinctBy {

                                it.applicationInfo.packageName
                            }
                            .sortedBy {

                                it.searchName
                            }
                    }


                // =================================================
                // COMPLETE CACHE SAVE
                // =================================================

                apps = result

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }


    // =========================================================
    // UPDATE ICON
    // =========================================================

    fun updateIcon(
        packageName: String,
        icon: androidx.compose.ui.graphics.ImageBitmap
    ) {

        val currentApps =
            apps ?: return

        apps =
            currentApps.map { appItem ->

                if (
                    appItem.applicationInfo.packageName ==
                    packageName
                ) {

                    appItem.copy(
                        iconBitmap = icon
                    )

                } else {

                    appItem
                }
            }
    }


    // =========================================================
    // CLEAR CACHE
    // =========================================================

    fun clear() {

        apps = null
    }
}


// =============================================================
// APP LIST SCREEN
// =============================================================

@Composable
fun AppListScreen(
    context: Context
) {

    val appContext =
        remember {
            context.applicationContext
        }


    val scope =
        rememberCoroutineScope()


    val dataStore =
        remember {
            DataStoreManager(
                appContext
            )
        }


    // =========================================================
    // TAB
    // =========================================================

    var selectedTab by
    remember {
        mutableStateOf(0)
    }


    // =========================================================
    // SEARCH
    // =========================================================

    var searchText by
    remember {
        mutableStateOf("")
    }


    // =========================================================
    // APPS
    // =========================================================

    var apps by
    remember {
        mutableStateOf(
            AppListCache.getApps()
                ?: emptyList()
        )
    }


    LaunchedEffect(Unit) {

        val cachedApps =
            AppListCache.getApps()


        if (cachedApps != null) {

            apps =
                cachedApps

        } else {

            AppListCache.preload(
                appContext
            )


            AppListCache.getApps()
                ?.let { loadedApps ->

                    apps =
                        loadedApps
                }
        }
    }


    // =========================================================
    // LOCKED APPS
    // =========================================================

    val lockedApps by
    dataStore.lockedAppsFlow.collectAsState(
        initial = emptySet()
    )


    // =========================================================
    // NORMALIZED SEARCH
    // =========================================================

    val normalizedSearch =
        remember(searchText) {

            searchText
                .trim()
                .lowercase()
        }


    // =========================================================
    // FILTERED APPS
    // =========================================================

    val filteredApps =
        remember(
            apps,
            lockedApps,
            selectedTab,
            normalizedSearch
        ) {

            val tabApps =

                if (selectedTab == 0) {

                    // UNLOCKED

                    apps.filter { appItem ->

                        !lockedApps.contains(
                            appItem
                                .applicationInfo
                                .packageName
                        )
                    }

                } else {

                    // LOCKED

                    apps.filter { appItem ->

                        lockedApps.contains(
                            appItem
                                .applicationInfo
                                .packageName
                        )
                    }
                }


            if (
                normalizedSearch.isEmpty()
            ) {

                tabApps

            } else {

                tabApps.filter { appItem ->

                    appItem.searchName.contains(
                        normalizedSearch
                    )
                }
            }
        }


    // =========================================================
    // MAIN UI
    // =========================================================

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.White
                )
    ) {


        // =====================================================
        // APP LOCK HEADING
        // =====================================================

        Text(

            text =
                stringResource(
                    R.string.app_lock
                ),

            color =
                Color.Black,

            fontSize =
                20.sp,

            modifier =
                Modifier.padding(
                    start = 20.dp,
                    top = 15.dp
                )
        )


        // =====================================================
        // TABS
        // =====================================================

        TabRow(

            selectedTabIndex =
                selectedTab,

            containerColor =
                Color.White,

            contentColor =
                Color(0xFF0396FF),

            modifier =
                Modifier.padding(
                    top = 18.dp
                ),

            indicator = { tabPositions ->

                if (
                    selectedTab <
                    tabPositions.size
                ) {

                    TabRowDefaults.Indicator(

                        modifier =
                            Modifier.tabIndicatorOffset(
                                tabPositions[
                                    selectedTab
                                ]
                            ),

                        color =
                            Color(0xFF0396FF),

                        height =
                            2.dp
                    )
                }
            }
        ) {


            // =================================================
            // UNLOCKED TAB
            // =================================================

            Tab(

                selected =
                    selectedTab == 0,

                onClick = {

                    selectedTab = 0
                },

                modifier =
                    Modifier.height(
                        42.dp
                    )
            ) {

                Row(

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.Center
                ) {

                    Image(

                        painter =
                            painterResource(
                                id =
                                    R.drawable.unlock
                            ),

                        contentDescription =
                            stringResource(
                                R.string.unlocked
                            ),

                        colorFilter =
                            ColorFilter.tint(

                                if (
                                    selectedTab == 0
                                ) {

                                    Color(
                                        0xFF0396FF
                                    )

                                } else {

                                    Color(
                                        0xFFBDBDBD
                                    )
                                }
                            ),

                        modifier =
                            Modifier.size(
                                18.dp
                            )
                    )


                    Spacer(
                        modifier =
                            Modifier.width(
                                5.dp
                            )
                    )


                    Text(

                        text =
                            stringResource(
                                R.string.unlocked
                            ),

                        color =
                            if (
                                selectedTab == 0
                            ) {

                                Color(
                                    0xFF0396FF
                                )

                            } else {

                                Color(
                                    0xFFBDBDBD
                                )
                            },

                        fontSize =
                            16.sp
                    )
                }
            }


            // =================================================
            // LOCKED TAB
            // =================================================

            Tab(

                selected =
                    selectedTab == 1,

                onClick = {

                    selectedTab = 1
                },

                modifier =
                    Modifier.height(
                        42.dp
                    )
            ) {

                Row(

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.Center
                ) {

                    Image(

                        painter =
                            painterResource(
                                id =
                                    R.drawable.locked
                            ),

                        contentDescription =
                            stringResource(
                                R.string.locked
                            ),

                        colorFilter =
                            ColorFilter.tint(

                                if (
                                    selectedTab == 1
                                ) {

                                    Color(
                                        0xFF0396FF
                                    )

                                } else {

                                    Color(
                                        0xFFBDBDBD
                                    )
                                }
                            ),

                        modifier =
                            Modifier.size(
                                18.dp
                            )
                    )


                    Spacer(
                        modifier =
                            Modifier.width(
                                5.dp
                            )
                    )


                    Text(

                        text =
                            stringResource(
                                R.string.locked
                            ),

                        color =
                            if (
                                selectedTab == 1
                            ) {

                                Color(
                                    0xFF0396FF
                                )

                            } else {

                                Color(
                                    0xFFBDBDBD
                                )
                            },

                        fontSize =
                            16.sp
                    )
                }
            }
        }


        // =====================================================
        // SEARCH BOX
        // =====================================================

        Box(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        top = 15.dp
                    )
                    .height(39.dp)
                    .background(

                        color =
                            Color(0xFFF7F7F7),

                        shape =
                            RoundedCornerShape(
                                22.dp
                            )
                    )
        ) {

            Row(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = 12.dp,
                            end = 12.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {


                Icon(

                    imageVector =
                        Icons.Default.Search,

                    contentDescription =
                        stringResource(
                            R.string.search
                        ),

                    tint =
                        Color(0xFFBDBDBD),

                    modifier =
                        Modifier.size(
                            19.dp
                        )
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )


                BasicTextField(

                    value =
                        searchText,

                    onValueChange = {

                        searchText =
                            it
                    },

                    singleLine =
                        true,

                    textStyle =
                        TextStyle(

                            color =
                                Color(0xFF555555),

                            fontSize =
                                14.sp
                        ),

                    cursorBrush =
                        SolidColor(
                            Color(0xFF0396FF)
                        ),

                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),

                    decorationBox = {
                            innerTextField ->

                        Box(

                            modifier =
                                Modifier.fillMaxSize(),

                            contentAlignment =
                                Alignment.CenterStart
                        ) {

                            if (
                                searchText.isEmpty()
                            ) {

                                Text(

                                    text =
                                        stringResource(
                                            R.string.search
                                        ),

                                    color =
                                        Color(
                                            0xFFBDBDBD
                                        ),

                                    fontSize =
                                        14.sp
                                )
                            }

                            innerTextField()
                        }
                    }
                )
            }
        }


        // =====================================================
        // SMALL SPACE
        // =====================================================

        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )


        // =====================================================
        // GENERAL
        // =====================================================

        Text(

            text =
                stringResource(
                    R.string.general
                ),

            color =
                Color(0xFF878585),

            fontSize =
                14.sp,

            modifier =
                Modifier.padding(
                    start = 18.dp,
                    top = 7.dp,
                    bottom = 3.dp
                )
        )


        // =====================================================
        // SPACE BEFORE APP LIST
        // =====================================================

        Spacer(
            modifier =
                Modifier.height(
                    18.dp
                )
        )


        // =====================================================
        // APP LIST
        // IMPORTANT:
        // weight(1f) gives LazyColumn remaining screen height
        // top padding gives first card room for complete shadow
        // =====================================================

        LazyColumn(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),

            verticalArrangement =
                Arrangement.spacedBy(
                    9.dp
                ),

            contentPadding =
                PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 4.dp,
                    bottom = 16.dp
                )
        ) {

            items(

                items =
                    filteredApps,

                key = { appItem ->

                    appItem
                        .applicationInfo
                        .packageName
                }

            ) { appItem ->


                val appName =
                    appItem.appName


                val packageName =
                    appItem
                        .applicationInfo
                        .packageName


                val isLocked =
                    lockedApps.contains(
                        packageName
                    )


                // =================================================
                // APP CARD
                // =================================================

                Row(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .shadow(

                                elevation =
                                    3.dp,

                                shape =
                                    RoundedCornerShape(
                                        8.dp
                                    )
                            )
                            .background(

                                color =
                                    Color.White,

                                shape =
                                    RoundedCornerShape(
                                        8.dp
                                    )
                            )
                            .padding(
                                start = 9.dp,
                                end = 5.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {


                    // =================================================
                    // APP ICON
                    // =================================================

                    if (
                        appItem.iconBitmap != null
                    ) {

                        Image(

                            bitmap =
                                appItem.iconBitmap,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(
                                    32.dp
                                )
                        )

                    } else {

                        Spacer(
                            modifier =
                                Modifier.size(
                                    32.dp
                                )
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.width(
                                9.dp
                            )
                    )


                    // =================================================
                    // APP NAME
                    // =================================================

                    Text(

                        text =
                            appName,

                        color =
                            Color(0xFF555555),

                        fontSize =
                            13.sp,

                        maxLines =
                            1,

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )


                    // =================================================
                    // LOCK / UNLOCK BUTTON
                    // =================================================

                    IconButton(

                        onClick = {

                            scope.launch {

                                if (
                                    isLocked
                                ) {

                                    dataStore
                                        .removeLockedApp(
                                            packageName
                                        )

                                } else {

                                    dataStore
                                        .saveLockedApp(
                                            packageName
                                        )
                                }
                            }
                        },

                        modifier =
                            Modifier.size(
                                32.dp
                            )
                    ) {

                        Image(

                            painter =
                                painterResource(

                                    id =
                                        if (
                                            isLocked
                                        ) {

                                            R.drawable.locked

                                        } else {

                                            R.drawable.unlock
                                        }
                                ),

                            contentDescription =

                                if (
                                    isLocked
                                ) {

                                    stringResource(
                                        R.string.unlock_app,
                                        appName
                                    )

                                } else {

                                    stringResource(
                                        R.string.lock_app,
                                        appName
                                    )
                                },

                            modifier =
                                Modifier.size(
                                    21.dp
                                )
                        )
                    }
                }
            }
        }
    }
}