package com.example.applock.Design.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.applock.R

@Composable
fun StartScreen(
    navController: NavController,
    appInitialized: Boolean
) {

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.loadingbar
        )
    )

    var navigationDone by remember {
        mutableStateOf(false)
    }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    LaunchedEffect(
        progress,
        appInitialized
    ) {

        if (
            composition != null &&
            progress >= 1f &&
            !navigationDone
        ) {

            navigationDone = true

            if (appInitialized) {

                navController.navigate(
                    "unlockScreen"
                ) {

                    popUpTo(
                        "startScreen"
                    ) {
                        inclusive = true
                    }

                    launchSingleTop = true
                }

            } else {

                navController.navigate(
                    "welcomeScreen"
                ) {

                    popUpTo(
                        "startScreen"
                    ) {
                        inclusive = true
                    }
                }
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.White.copy(
                    alpha = 0.9f
                )
            )
    ) {

        // =============================================================
        // TOP / CENTER CONTENT
        // =============================================================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Image(
                painter =
                    painterResource(
                        id = R.drawable.startscreen
                    ),

                contentDescription =
                    "App Lock",

                modifier =
                    Modifier.size(
                        270.dp
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )


            Text(
                text =
                    buildAnnotatedString {

                        withStyle(
                            style =
                                SpanStyle(
                                    color =
                                        Color(0xFF333333),

                                    fontWeight =
                                        FontWeight.Bold
                                )
                        ) {
                            append("App ")
                        }

                        withStyle(
                            style =
                                SpanStyle(
                                    color =
                                        Color(0xFF2196F3),

                                    fontWeight =
                                        FontWeight.Bold
                                )
                        ) {
                            append("Lock")
                        }
                    },

                fontSize =
                    34.sp
            )


            Spacer(
                modifier =
                    Modifier.height(
                        9.dp
                    )
            )


            Text(
                text =
                    "Secure your apps. Protect your privacy.",

                fontSize =
                    13.sp,

                color =
                    Color.Gray
            )
        }


        // =============================================================
        // BOTTOM CONTENT
        // =============================================================

        Column(
            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.navigationBars
                )
                .padding(
                    start = 100.dp,
                    end = 100.dp,
                    bottom = 20.dp
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            LottieAnimation(
                composition =
                    composition,

                progress = {
                    progress
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            25.dp
                        )
            )


            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )


            Text(
                text =
                    "This action may contain ads",

                fontSize =
                    14.sp,

                color =
                    Color.Gray
            )
        }
    }
}