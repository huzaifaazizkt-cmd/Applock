package com.example.applock.Design.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.applock.R


// =============================================================
// VAULT SCREEN
// =============================================================

@Composable
fun VaultScreen() {

    // =========================================================
    // 0 = PHOTOS
    // 1 = VIDEOS
    // =========================================================

    var selectedTab by remember {
        mutableStateOf(0)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // =====================================================
        // MAIN CONTENT
        // =====================================================

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // =================================================
            // VAULT HEADING
            // =================================================

            Text(
                text = "Vault",

                color = Color.Black,

                fontSize = 20.sp,

                modifier = Modifier
                    .padding(
                        start = 60.dp,
                        top = 10.dp
                    )
            )


            // =================================================
            // PHOTOS / VIDEOS TAB ROW
            //
            // AppListScreen ke Unlocked / Locked jaisa
            // =================================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 18.dp
                    )
                    .height(42.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                // =============================================
                // PHOTOS TAB
                // =============================================

                VaultTab(
                    selected = selectedTab == 0,

                    icon = R.drawable.imageicon,

                    text = "Photos",

                    onClick = {
                        selectedTab = 0
                    },

                    modifier = Modifier
                        .weight(1f)
                )


                // =============================================
                // VIDEOS TAB
                // =============================================

                VaultTab(
                    selected = selectedTab == 1,

                    icon = R.drawable.vedioicon,

                    text = "Videos",

                    onClick = {
                        selectedTab = 1
                    },

                    modifier = Modifier
                        .weight(1f)
                )
            }


            // =================================================
            // CONTENT
            // =================================================

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                when (selectedTab) {

                    // =========================================
                    // PHOTOS
                    // =========================================

                    0 -> {

                        PhotosVaultContent()
                    }


                    // =========================================
                    // VIDEOS
                    // =========================================

                    1 -> {

                        VideosVaultContent()
                    }
                }
            }
        }


        // =====================================================
        // PLUS BUTTON
        //
        // Bottom navigation se 50dp uper
        // =====================================================
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 15.dp,
                    bottom = 29.dp
                )
                .size(50.dp)
                .clip(CircleShape)
                .clickable {

                    // Yahan baad mein image/video picker open karenge.

                },

            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.plus
                ),

                contentDescription = "Add",

                modifier = Modifier.size(50.dp)
            )
        }
    }
}



@Composable
private fun VaultTab(
    selected: Boolean,
    icon: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val selectedColor =
        Color(0xFF0396FF)

    val unselectedColor =
        Color(0xFFBDBDBD)


    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable {
                onClick()
            },

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        // =====================================================
        // ICON + TEXT
        // =====================================================

        Row(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.Center
        ) {

            // =================================================
            // ICON
            // =================================================

            Image(
                painter =
                    painterResource(
                        id = icon
                    ),

                contentDescription =
                    text,

                colorFilter =
                    ColorFilter.tint(
                        if (selected) {
                            selectedColor
                        } else {
                            unselectedColor
                        }
                    ),

                modifier =
                    Modifier.size(18.dp)
            )


            Spacer(
                modifier =
                    Modifier.width(5.dp)
            )


            // =================================================
            // TEXT
            // =================================================

            Text(
                text = text,

                color =
                    if (selected) {
                        selectedColor
                    } else {
                        unselectedColor
                    },

                fontSize =
                    16.sp
            )
        }


        // =====================================================
        // SELECTED TAB LINE
        // =====================================================

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    if (selected) {
                        selectedColor
                    } else {
                        Color.Transparent
                    }
                )
        )
    }
}


// =============================================================
// PHOTOS CONTENT
// =============================================================

@Composable
private fun PhotosVaultContent() {

    Box(
        modifier = Modifier
            .fillMaxSize(),

        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            // =================================================
            // IMAGE CLICK
            // =================================================

            Image(
                painter =
                    painterResource(
                        id = R.drawable.imageclick
                    ),

                contentDescription =
                    "Add Image",

                modifier =
                    Modifier.size(80.dp)
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            // =================================================
            // TEXT
            // =================================================

            Text(
                text = """Click "+" To add image""",

                color =
                    Color(0xFF9E9E9E),

                fontSize =
                    14.sp
            )
        }
    }
}


// =============================================================
// VIDEOS CONTENT
// =============================================================

@Composable
private fun VideosVaultContent() {

    Box(
        modifier = Modifier
            .fillMaxSize(),

        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            // =================================================
            // VIDEO CLICK ICON
            //
            // IMPORTANT:
            // Yahan vedioicon nahi,
            // aapka vedioClick use hoga.
            // =================================================

            Image(
                painter =
                    painterResource(
                        id = R.drawable.vedioclick
                    ),

                contentDescription =
                    "Add Video",

                modifier =
                    Modifier.size(80.dp)
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            // =================================================
            // TEXT
            // =================================================

            Text(
                text = """Click "+" To add video""",

                color =
                    Color(0xFF9E9E9E),

                fontSize =
                    14.sp
            )
        }
    }
}