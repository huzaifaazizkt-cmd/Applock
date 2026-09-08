package com.example.applock.Design.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.applock.R

@Composable
fun ExitScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var selectedRating by remember {
        mutableIntStateOf(0)
    }


    BackHandler {
        navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // ------------------------------------------------
        // TOP BAR
        // ------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(
                    horizontal = 16.dp,

                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF3D3D3D),
                modifier = Modifier
                    .size(28.dp)
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null
                    ) {
                        navController.popBackStack()
                    }
            )

            Spacer(
                modifier = Modifier.width(18.dp)
            )

            Text(
                text = "Exit App lock",
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF3D3D3D)
            )
        }
        // ------------------------------------------------
        // CONTENT
        // ------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            // EXIT IMAGE
            Image(
                painter = painterResource(
                    id = R.drawable.exitlock
                ),
                contentDescription = "Exit App Lock",
                modifier = Modifier.size(200.dp)
            )

            Spacer(
                modifier = Modifier.height(34.dp)
            )

            // TITLE
            Text(
                text = "Exit App Lock?",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF383838)
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // DESCRIPTION
            Text(
                text = "You will need to enter your passcode\nagain to access App Lock.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFF666666),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            // ------------------------------------------------
            // RATING STARS
            // ------------------------------------------------
            Row(
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                for (index in 1..5) {

                    val isSelected = index <= selectedRating

                    Text(
                        text = if (isSelected) "★" else "☆",
                        fontSize = 39.sp,
                        color = if (isSelected) {
                            Color(0xFF2196F3)
                        } else {
                            Color(0xFF444444)
                        },
                        modifier = Modifier
                            .size(39.dp)
                            .clickable(
                                interactionSource = remember {
                                    MutableInteractionSource()
                                },
                                indication = null
                            ) {
                                selectedRating =
                                    if (selectedRating == index) 0 else index
                            }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // RATING DESCRIPTION
            Text(
                text = "A Quick rating from you means\n a lot to our team.",
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(26.dp)
            )

            // ------------------------------------------------
            // STAY PROTECTED BUTTON
            // ------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(51.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(
                        Color(0xFF2196F3)
                    )
                    .clickable {


                        navController.popBackStack()
                    },
                contentAlignment = Alignment.Center
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    Image(
                        painter = painterResource(
                            id = R.drawable.stay
                        ),
                        contentDescription = "Stay Protected",
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Text(
                        text = "Stay Protected",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(17.dp)
            )

            // ------------------------------------------------
            // EXIT ANYWAY BUTTON
            // ------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(51.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF2196F3),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable {

                        navController.popBackStack()
                        activity?.moveTaskToBack(true)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    Image(
                        painter = painterResource(
                            id = R.drawable.anyway
                        ),
                        contentDescription = "Exit Anyway",
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Text(
                        text = "Exit Anyway",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2196F3)
                    )
                }
            }
            Spacer(
                modifier = Modifier.height(30.dp)
            )
        }
    }
}