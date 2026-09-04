
package com.example.applock.Design.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.applock.R
import com.example.applock.Design.screens.AppListScreen
import com.example.applock.Design.screens.SettingsScreen
import com.example.applock.Design.screens.VaultScreen

// =============================================================
// MAIN SCREEN
// =============================================================

@Composable
fun MainScreen(

    context: Context,

    onIntruderClick: () -> Unit,

    onLanguageClick: () -> Unit,

    openSettings: Boolean = false

) {

    // =========================================================
    // SELECTED TAB
    // =========================================================

    var selectedTab by remember {

        mutableStateOf(

            if (openSettings) {

                2

            } else {

                0
            }
        )
    }


    // =========================================================
    // WHEN OPEN SETTINGS CHANGES
    // =========================================================

    LaunchedEffect(openSettings) {

        if (openSettings) {

            selectedTab = 2
        }
    }


    // =========================================================
    // ROOT
    // =========================================================

    Box(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.White
                )
    ) {


        // =====================================================
        // MAIN CONTENT
        // =====================================================

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = 74.dp
                    )
        ) {

            when (selectedTab) {


                // =================================================
                // 0 = APPS LOCK
                // =================================================

                0 -> {

                    AppListScreen(

                        context =
                            context
                    )
                }


                // =================================================
                // 1 = VAULT
                // =================================================

                1 -> {

                    VaultScreen()
                }


                // =================================================
                // 2 = SETTINGS
                // =================================================

                2 -> {

                    SettingsScreen(

                        // ---------------------------------------------
                        // INTRUDER
                        // ---------------------------------------------

                        onIntruderClick =
                            onIntruderClick,


                        // ---------------------------------------------
                        // LANGUAGES
                        // ---------------------------------------------

                        onLanguageClick =
                            onLanguageClick
                    )
                }
            }
        }


        // =====================================================
        // BOTTOM NAVIGATION
        // =====================================================

        AppLockBottomNavigation(

            selectedTab =
                selectedTab,

            onTabSelected = { tab ->

                selectedTab =
                    tab
            },

            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
        )
    }
}


// =============================================================
// BOTTOM NAVIGATION
// =============================================================

@Composable
private fun AppLockBottomNavigation(

    selectedTab: Int,

    onTabSelected: (Int) -> Unit,

    modifier: Modifier = Modifier

) {

    Row(

        modifier =
            modifier
                .fillMaxWidth()
                .height(74.dp)
                .background(
                    Color.White
                )
                .padding(
                    horizontal = 28.dp,
                    vertical = 10.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {


        // =====================================================
        // APPS LOCK
        // =====================================================

        BottomNavigationItem(

            selected =
                selectedTab == 0,

            icon =
                R.drawable.group6,

            text =
                stringResource(
                    R.string.apps_lock
                ),

            onClick = {

                onTabSelected(
                    0
                )
            }
        )


        // =====================================================
        // VAULT
        // =====================================================

        BottomNavigationItem(

            selected =
                selectedTab == 1,

            icon =
                R.drawable.group8,

            text =
                stringResource(
                    R.string.vault
                ),

            onClick = {

                onTabSelected(
                    1
                )
            }
        )


        // =====================================================
        // SETTINGS
        // =====================================================

        BottomNavigationItem(

            selected =
                selectedTab == 2,

            icon =
                R.drawable.group7,

            text =
                stringResource(
                    R.string.settings
                ),

            onClick = {

                onTabSelected(
                    2
                )
            }
        )
    }
}


// =============================================================
// SINGLE BOTTOM NAVIGATION ITEM
// =============================================================

@Composable
private fun BottomNavigationItem(

    selected: Boolean,

    icon: Int,

    text: String,

    onClick: () -> Unit

) {

    val blueColor =
        Color(0xFF2196F3)

    val grayColor =
        Color(0xFFBDBDBD)


    Box(

        modifier =
            Modifier
                .height(
                    38.dp
                )

                .clickable {

                    onClick()
                }

                .background(

                    color =
                        if (selected) {

                            blueColor

                        } else {

                            Color.Transparent
                        },

                    shape =
                        RoundedCornerShape(
                            22.dp
                        )
                )

                .padding(

                    horizontal =
                        if (selected) {

                            12.dp

                        } else {

                            5.dp
                        }
                ),

        contentAlignment =
            Alignment.Center
    ) {


        Row(

            verticalAlignment =
                Alignment.CenterVertically
        ) {


            // =================================================
            // ICON
            // =================================================

            Image(

                painter =
                    painterResource(
                        id =
                            icon
                    ),

                contentDescription =
                    text,

                colorFilter =
                    ColorFilter.tint(

                        if (selected) {

                            Color.White

                        } else {

                            grayColor
                        }
                    ),

                modifier =
                    Modifier
                        .size(
                            22.dp
                        )
                        .alpha(

                            if (selected) {

                                1f

                            } else {

                                0.9f
                            }
                        )
            )


            // =================================================
            // SPACE
            // =================================================

            Spacer(

                modifier =
                    Modifier.width(
                        5.dp
                    )
            )


            // =================================================
            // TEXT
            // =================================================

            Text(

                text =
                    text,

                color =
                    if (selected) {

                        Color.White

                    } else {

                        grayColor
                    },

                fontSize =
                    12.sp
            )
        }
    }
}

