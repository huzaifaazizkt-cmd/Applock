package com.example.applock.Design.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.applock.R
import kotlinx.coroutines.delay

@Composable
fun StartScreen(
    navController: NavController
) {

    var progress by remember {
        mutableFloatStateOf(0f)
    }

    // Splash loading
    LaunchedEffect(Unit) {

        progress = 1f

        // 2.5 seconds loading
        delay(3500)

        // Loading ke baad Welcome Screen
        navController.navigate("welcomeScreen") {
            popUpTo("startScreen") {
                inclusive = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {


        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Image(
                painter = painterResource(
                    id = R.drawable.startscreen
                ),
                contentDescription = "App Lock",
                modifier = Modifier.size(270.dp)
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )


            Text(
                text = "App Lock",
                style = TextStyle(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(
                modifier = Modifier.height(9.dp)
            )


            Text(
                text = "Secure your apps. Protect your privacy.",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }



        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = 45.dp,
                    end = 45.dp,
                    bottom = 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(
                        RoundedCornerShape(20.dp)
                    )
                    .background(
                        Color(0xFFE1E5E9)
                    )
            ) {


                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .clip(
                            RoundedCornerShape(20.dp)
                        )
                        .background(
                            Color(0xFF2196F3)
                        )
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "This action may contain ads",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}