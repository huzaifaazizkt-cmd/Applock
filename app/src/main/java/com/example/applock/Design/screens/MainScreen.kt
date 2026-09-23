package com.example.applock.Design.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.applock.R

@Composable
fun MainScreen(
    context: Context,
    onIntruderClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onResetPasswordClick: () -> Unit,
    openSettings: Boolean = false,
    navController: NavController
) {
    val savedStateHandle =
        navController.currentBackStackEntry
            ?.savedStateHandle

    var selectedTab by remember {
        mutableIntStateOf(
            if (openSettings) {
                2
            } else {
                0
            }
        )
    }

    var isVaultPreviewOpen by remember {
        mutableStateOf(false)
    }

    var settingsPreviousTab by remember {
        mutableIntStateOf(
            savedStateHandle
                ?.get<Int>("settingsPreviousTab")
                ?: 0
        )
    }

    val forceAppList =
        savedStateHandle
            ?.get<Boolean>("forceAppList")
            ?: false

    val returnToSettings =
        savedStateHandle
            ?.get<Boolean>("returnToSettings")
            ?: false

    LaunchedEffect(
        openSettings,
        forceAppList,
        returnToSettings
    ) {
        when {
            forceAppList -> {
                selectedTab = 0
                settingsPreviousTab = 0

                savedStateHandle?.set(
                    "settingsPreviousTab",
                    0
                )

                savedStateHandle?.remove<Boolean>(
                    "forceAppList"
                )

                savedStateHandle?.remove<Boolean>(
                    "openSettings"
                )

                savedStateHandle?.remove<Boolean>(
                    "returnToSettings"
                )
            }

            returnToSettings -> {
                selectedTab = 2

                savedStateHandle?.remove<Boolean>(
                    "returnToSettings"
                )
            }

            openSettings -> {
                selectedTab = 2

                savedStateHandle?.remove<Boolean>(
                    "openSettings"
                )
            }
        }
    }

    fun selectTab(tab: Int) {
        if (tab == selectedTab) {
            return
        }

        if (tab == 2) {
            if (selectedTab == 0 || selectedTab == 1) {
                settingsPreviousTab = selectedTab

                savedStateHandle?.set(
                    "settingsPreviousTab",
                    selectedTab
                )
            }
        }

        isVaultPreviewOpen = false
        selectedTab = tab
    }

    BackHandler {
        if (isVaultPreviewOpen) {
            isVaultPreviewOpen = false
            return@BackHandler
        }

        when {
            selectedTab == 2 -> {
                val previousTab =
                    savedStateHandle
                        ?.get<Int>("settingsPreviousTab")
                        ?: settingsPreviousTab

                selectedTab =
                    if (previousTab == 1) {
                        1
                    } else {
                        0
                    }
            }

            selectedTab == 1 -> {
                selectedTab = 0

                savedStateHandle?.set(
                    "settingsPreviousTab",
                    0
                )
            }

            selectedTab == 0 -> {
                navController.navigate("exit")
            }
        }
    }

    val navigationBarBottomPadding =
        WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isVaultPreviewOpen) {
                        Modifier
                    } else {
                        Modifier.padding(
                            bottom = 74.dp + navigationBarBottomPadding
                        )
                    }
                )
        ) {
            when (selectedTab) {
                0 -> {
                    AppListScreen(
                        context = context
                    )
                }

                1 -> {
                    VaultScreen(
                        onPreviewStateChange = { isOpen ->
                            isVaultPreviewOpen = isOpen
                        }
                    )
                }

                2 -> {
                    SettingsScreen(
                        onIntruderClick = {
                            onIntruderClick()
                        },
                        onLanguageClick = {
                            onLanguageClick()
                        },
                        onResetPasswordClick =
                            onResetPasswordClick
                    )
                }
            }
        }

        if (!isVaultPreviewOpen) {
            AppLockBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectTab(tab)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(
                        WindowInsets.navigationBars
                    )
            )
        }
    }
}

@Composable
private fun AppLockBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .background(Color.White)
            .padding(
                horizontal = 28.dp,
                vertical = 10.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavigationItem(
            selected = selectedTab == 0,
            icon = R.drawable.group6,
            text = stringResource(R.string.apps_lock),
            onClick = {
                onTabSelected(0)
            }
        )

        BottomNavigationItem(
            selected = selectedTab == 1,
            icon = R.drawable.group8,
            text = stringResource(R.string.vault),
            onClick = {
                onTabSelected(1)
            }
        )

        BottomNavigationItem(
            selected = selectedTab == 2,
            icon = R.drawable.group7,
            text = stringResource(R.string.settings),
            onClick = {
                onTabSelected(2)
            }
        )
    }
}

@Composable
private fun BottomNavigationItem(
    selected: Boolean,
    icon: Int,
    text: String,
    onClick: () -> Unit
) {
    val blueColor = Color(0xFF2196F3)
    val grayColor = Color(0xFFBDBDBD)

    Box(
        modifier = Modifier
            .height(38.dp)
            .clickable(
                indication = null,
                interactionSource = remember {
                    MutableInteractionSource()
                }
            ) {
                onClick()
            }
            .background(
                color = if (selected) { blueColor } else { Color.Transparent },
                shape = RoundedCornerShape(22.dp)
            )
            .padding(
                horizontal = if (selected) { 12.dp } else { 5.dp }),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = text,
                colorFilter = ColorFilter.tint(
                    if (selected) { Color.White } else { grayColor }),
                modifier = Modifier
                    .size(22.dp)
                    .alpha(if (selected) { 1f } else { 0.9f })
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = text,
                color = if (selected) { Color.White } else { grayColor },
                fontSize = 12.sp
            )
        }
    }
}