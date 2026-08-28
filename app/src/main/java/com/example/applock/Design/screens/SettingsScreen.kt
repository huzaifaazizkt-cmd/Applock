package com.example.applock.Design.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.applock.R


@Composable
fun SettingsScreen() {

    // =========================================================
    // SETTINGS ITEMS
    // =========================================================

    val settingsItems = listOf(

        Triple(
            "Lock Setting",
            R.drawable.settingicon,
            "Lock settings"
        ),

        Triple(
            "Intruder",
            R.drawable.intruder,
            "Intruder settings"
        ),

        Triple(
            "Hide Settings",
            R.drawable.hideicon,
            "Hide settings"
        ),

        Triple(
            "Contact Us",
            R.drawable.messageicon,
            "Contact us"
        ),

        Triple(
            "Update",
            R.drawable.update,
            "Check for updates"
        ),

        Triple(
            "About",
            R.drawable.about,
            "About AppLock"
        ),

        Triple(
            "Languages",
            R.drawable.language,
            "Select language"
        )
    )

    // =========================================================
    // EXPANDED ITEM
    // =========================================================

    var expandedItem by remember {
        mutableStateOf<String?>(null)
    }


    // =========================================================
    // MAIN SCREEN
    // =========================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF7F7F7)
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    top = 22.dp
                )
        ) {

            // =================================================
            // SETTINGS TITLE
            // =================================================

            Text(
                text = "Settings",

                modifier = Modifier
                    .padding(
                        start = 40.dp
                    ),

                color = Color(0xFF333333),

                fontSize = 22.sp
            )


            // =================================================
            // TITLE / FIRST CARD SPACE
            // =================================================

            Spacer(
                modifier = Modifier
                    .height(22.dp)
            )


            // =================================================
            // SETTINGS LIST
            // =================================================

            settingsItems.forEach { item ->

                val title =
                    item.first

                val icon =
                    item.second

                val description =
                    item.third


                SettingsItem(

                    title = title,

                    iconRes = icon,

                    description = description,

                    expanded =
                        expandedItem == title,

                    onClick = {

                        expandedItem =
                            if (
                                expandedItem == title
                            ) {

                                null

                            } else {

                                title
                            }
                    }
                )


                // =================================================
                // 12 DP SPACE BETWEEN CARDS
                // =================================================

                Spacer(
                    modifier = Modifier
                        .height(12.dp)
                )
            }
        }
    }
}


// =============================================================
// SETTINGS ITEM
// =============================================================

@Composable
private fun SettingsItem(
    title: String,
    iconRes: Int,
    description: String,
    expanded: Boolean,
    onClick: () -> Unit
) {

    Card(

        modifier = Modifier

            .fillMaxWidth()

            // =================================================
            // VISIBLE SHADOW
            // =================================================

            .shadow(
                elevation = 5.dp,

                shape =
                    RoundedCornerShape(
                        12.dp
                    ),

                clip = false,

                ambientColor =
                    Color.Black.copy(
                        alpha = 0.10f
                    ),

                spotColor =
                    Color.Black.copy(
                        alpha = 0.10f
                    )
            )

            // =================================================
            // ROUNDED CORNER
            // =================================================

            .clip(
                RoundedCornerShape(
                    12.dp
                )
            )

            // =================================================
            // CLICK
            // =================================================

            .clickable {
                onClick()
            },


        shape =
            RoundedCornerShape(
                12.dp
            ),


        // =====================================================
        // PURE WHITE CARD
        // =====================================================

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),


        // =====================================================
        // CARD ELEVATION
        // =====================================================

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,

                pressedElevation = 1.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            // =================================================
            // MAIN ROW
            // =================================================

            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(
                        start = 8.dp,
                        end = 10.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                // =================================================
                // ICON
                // =================================================

                Image(

                    painter =
                        painterResource(
                            id = iconRes
                        ),

                    contentDescription =
                        title,

                    modifier = Modifier
                        .size(38.dp),

                    contentScale =
                        ContentScale.FillBounds
                )


                // =================================================
                // ICON TO TEXT SPACE
                // =================================================

                Spacer(
                    modifier = Modifier
                        .width(18.dp)
                )


                // =================================================
                // TITLE
                // =================================================

                Text(

                    text = title,

                    modifier = Modifier
                        .weight(1f),

                    color =
                        Color(0xFF333333),

                    fontSize = 15.sp
                )


                // =================================================
                // DOWN ICON
                // =================================================

                Image(

                    painter =
                        painterResource(
                            id = R.drawable.downicon
                        ),

                    contentDescription =
                        "Expand",

                    modifier = Modifier
                        .size(10.dp)
                        .clickable {
                            onClick()
                        },

                    contentScale =
                        ContentScale.Fit
                )
            }


            // =================================================
            // EXPANDED CONTENT
            // =================================================

            if (expanded) {

                Text(

                    text = description,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 64.dp,
                            end = 20.dp,
                            bottom = 14.dp
                        ),

                    color =
                        Color(0xFF666666),

                    fontSize = 13.sp
                )
            }
        }
    }
}