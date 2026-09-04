package com.example.applock.Design.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.applock.R
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    navController: NavController
) {

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = {
            3
        }
    )

    val coroutineScope = rememberCoroutineScope()

    val blueColor = Color(0xFF2196F3)


    val titles = listOf(
        "Secure Your Apps",
        "Quick & Secure Access",
        "Your Privacy, Protected"
    )

    val descriptions = listOf(
        "Keep your private apps protected with a secure passcode and an extra layer of privacy",
        "Unlock your protected apps instantly with your passcode or fingerprint",
        "Choose the apps you want to lock and keep your personal content safe from unwanted access"
    )

    val images = listOf(
        R.drawable.onboarding0,
        R.drawable.onboarding1,
        R.drawable.onboarding2
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF7F9FE)
            )
    ) {



        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
        ) { page ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 30.dp,
                        bottom = 175.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                Image(
                    painter = painterResource(
                        id = images[page]
                    ),
                    contentDescription = "Onboarding ${page + 1}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(330.dp)
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )


                Text(
                    text = titles[page],
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )



                Text(
                    text = descriptions[page],
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                )
            }
        }




        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 35.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {



            Button(
                onClick = {

                    if (pagerState.currentPage < 2) {

                        // Next page
                        coroutineScope.launch {

                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }

                    } else {

                        // Last page -> Pin Create
                        navController.navigate("create") {

                            popUpTo("onboardingScreen") {
                                inclusive = true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(51.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = blueColor
                )
            ) {

                Text(
                    text =
                        if (pagerState.currentPage == 2) {
                            "Get Started"
                        } else {
                            "Next"
                        },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )


            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                repeat(3) { index ->

                    Box(
                        modifier = Modifier
                            .size(
                                if (pagerState.currentPage == index) {
                                    8.dp
                                } else {
                                    8.dp
                                }
                            )
                            .clip(
                                RoundedCornerShape(50)
                            )
                            .background(
                                if (pagerState.currentPage == index) {
                                    blueColor
                                } else {
                                    Color(0xFFB9DDF8)
                                }
                            )
                    )
                }
            }
        }
    }
}
