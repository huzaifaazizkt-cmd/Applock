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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.applock.R

@Composable
fun WelcomeScreen(
    navController: NavController
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.White.copy(alpha = 0.9f)
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.startscreen
                ),

                contentDescription = "App Lock",

                modifier =
                    Modifier.size(270.dp)
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = buildAnnotatedString {

                    withStyle(
                        style = SpanStyle(
                            color =
                                Color(0xFF333333),

                            fontWeight =
                                FontWeight.Bold
                        )
                    ) {
                        append("App ")
                    }

                    withStyle(
                        style = SpanStyle(
                            color =
                                Color(0xFF2196F3),

                            fontWeight =
                                FontWeight.Bold
                        )
                    ) {
                        append("Lock")
                    }
                },

                fontSize = 34.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    "Secure your apps. Protect your privacy.",

                fontSize = 13.sp,

                color = Color.Gray
            )
        }


        Button(
            onClick = {

                navController.navigate(
                    "languagesSetup"
                ) {

                    popUpTo("welcomeScreen") {
                        inclusive = true
                    }
                }
            },

            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    bottom = 18.dp
                )
                .height(51.dp),

            shape =
                RoundedCornerShape(14.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF2196F3)
                )
        ) {

            Text(
                text = "Get Started",

                fontSize = 17.sp,

                fontWeight =
                    FontWeight.SemiBold,

                color = Color.White
            )
        }
    }
}