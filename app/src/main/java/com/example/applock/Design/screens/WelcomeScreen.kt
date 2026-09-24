
package com.example.applock.Design.screens

import android.app.Activity
import android.util.Log

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import com.example.applock.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    navController: NavController
) {

    val context = LocalContext.current
    val activity = context as? Activity

    val analytics = remember {
        FirebaseAnalytics.getInstance(context)
    }

    /*
     * Official Google test Interstitial Ad Unit ID.
     */
    val interstitialAdUnitId =
        "ca-app-pub-3940256099942544/1033173712"

    /*
     * Google AdMob detected this device as a test device.
     */
    val testDeviceId =
        "E5CA263188C38AF6BD96C4C84E9600BB"

    var isLoadingAd by remember {
        mutableStateOf(false)
    }

    var navigationDone by remember {
        mutableStateOf(false)
    }

    fun goToLanguages() {

        if (navigationDone) {
            return
        }

        navigationDone = true

        navController.navigate("languagesSetup") {

            popUpTo("welcomeScreen") {
                inclusive = true
            }
        }
    }

    if (isLoadingAd) {

        BackHandler {
            // Back disabled while loading/showing ad.
        }

        LaunchedEffect(Unit) {

            analytics.logEvent(
                "interstitial_ad_loading"
            ) {

                param(
                    "ad_type",
                    "interstitial"
                )

                param(
                    "ad_unit_id",
                    interstitialAdUnitId
                )

                param(
                    "screen",
                    "welcome_screen"
                )
            }

            /*
             * =====================================================
             * CONFIGURE THIS DEVICE AS A TEST DEVICE
             * =====================================================
             */

            val requestConfiguration =
                RequestConfiguration.Builder()
                    .setTestDeviceIds(
                        listOf(testDeviceId)
                    )
                    .build()

            MobileAds.setRequestConfiguration(
                requestConfiguration
            )

            /*
             * =====================================================
             * INITIALIZE ADMOB
             * =====================================================
             */

            MobileAds.initialize(context) {

                if (navigationDone) {
                    return@initialize
                }

                Log.d(
                    "AppLockAdMob",
                    "AdMob initialized successfully"
                )

                /*
                 * =================================================
                 * CREATE AD REQUEST
                 * =================================================
                 */

                val adRequest =
                    AdRequest.Builder()
                        .build()

                Log.d(
                    "AppLockAdMob",
                    "Loading test interstitial ad..."
                )

                /*
                 * =================================================
                 * LOAD INTERSTITIAL
                 * =================================================
                 */

                InterstitialAd.load(
                    context,
                    interstitialAdUnitId,
                    adRequest,
                    object : InterstitialAdLoadCallback() {

                        override fun onAdLoaded(
                            interstitialAd: InterstitialAd
                        ) {

                            if (navigationDone) {
                                return
                            }

                            Log.d(
                                "AppLockAdMob",
                                "AD LOADED SUCCESSFULLY"
                            )

                            analytics.logEvent(
                                "interstitial_ad_loaded"
                            ) {

                                param(
                                    "ad_type",
                                    "interstitial"
                                )

                                param(
                                    "ad_unit_id",
                                    interstitialAdUnitId
                                )

                                param(
                                    "screen",
                                    "welcome_screen"
                                )
                            }

                            /*
                             * =====================================
                             * FULL SCREEN CALLBACK
                             * =====================================
                             */

                            interstitialAd.fullScreenContentCallback =
                                object : FullScreenContentCallback() {

                                    override fun onAdShowedFullScreenContent() {

                                        Log.d(
                                            "AppLockAdMob",
                                            "AD SHOWN"
                                        )

                                        analytics.logEvent(
                                            "interstitial_ad_shown"
                                        ) {

                                            param(
                                                "ad_type",
                                                "interstitial"
                                            )

                                            param(
                                                "ad_unit_id",
                                                interstitialAdUnitId
                                            )

                                            param(
                                                "screen",
                                                "welcome_screen"
                                            )
                                        }
                                    }

                                    override fun onAdDismissedFullScreenContent() {

                                        Log.d(
                                            "AppLockAdMob",
                                            "AD CLOSED"
                                        )

                                        analytics.logEvent(
                                            "interstitial_ad_closed"
                                        ) {

                                            param(
                                                "ad_type",
                                                "interstitial"
                                            )

                                            param(
                                                "ad_unit_id",
                                                interstitialAdUnitId
                                            )

                                            param(
                                                "screen",
                                                "welcome_screen"
                                            )
                                        }

                                        goToLanguages()
                                    }

                                    override fun onAdFailedToShowFullScreenContent(
                                        adError: AdError
                                    ) {

                                        Log.e(
                                            "AppLockAdMob",
                                            "AD FAILED TO SHOW"
                                        )

                                        Log.e(
                                            "AppLockAdMob",
                                            "Code = ${adError.code}"
                                        )

                                        Log.e(
                                            "AppLockAdMob",
                                            "Message = ${adError.message}"
                                        )

                                        Log.e(
                                            "AppLockAdMob",
                                            "Domain = ${adError.domain}"
                                        )

                                        analytics.logEvent(
                                            "interstitial_ad_show_failed"
                                        ) {

                                            param(
                                                "ad_type",
                                                "interstitial"
                                            )

                                            param(
                                                "ad_unit_id",
                                                interstitialAdUnitId
                                            )

                                            param(
                                                "error_code",
                                                adError.code.toString()
                                            )

                                            param(
                                                "error_message",
                                                adError.message
                                            )

                                            param(
                                                "screen",
                                                "welcome_screen"
                                            )
                                        }

                                        goToLanguages()
                                    }
                                }

                            /*
                             * =====================================
                             * SHOW AD
                             * =====================================
                             */

                            if (
                                activity != null &&
                                !activity.isFinishing &&
                                !activity.isDestroyed
                            ) {

                                Log.d(
                                    "AppLockAdMob",
                                    "Showing interstitial ad..."
                                )

                                interstitialAd.show(
                                    activity
                                )

                            } else {

                                Log.e(
                                    "AppLockAdMob",
                                    "Activity is not available"
                                )

                                analytics.logEvent(
                                    "interstitial_ad_show_failed"
                                ) {

                                    param(
                                        "ad_type",
                                        "interstitial"
                                    )

                                    param(
                                        "ad_unit_id",
                                        interstitialAdUnitId
                                    )

                                    param(
                                        "error_message",
                                        "Activity is not available"
                                    )

                                    param(
                                        "screen",
                                        "welcome_screen"
                                    )
                                }

                                goToLanguages()
                            }
                        }

                        /*
                         * =========================================
                         * AD FAILED TO LOAD
                         * =========================================
                         */

                        override fun onAdFailedToLoad(
                            loadAdError: LoadAdError
                        ) {

                            Log.e(
                                "AppLockAdMob",
                                "================================"
                            )

                            Log.e(
                                "AppLockAdMob",
                                "AD FAILED TO LOAD"
                            )

                            Log.e(
                                "AppLockAdMob",
                                "Code = ${loadAdError.code}"
                            )

                            Log.e(
                                "AppLockAdMob",
                                "Message = ${loadAdError.message}"
                            )

                            Log.e(
                                "AppLockAdMob",
                                "Domain = ${loadAdError.domain}"
                            )

                            Log.e(
                                "AppLockAdMob",
                                "ResponseInfo = ${loadAdError.responseInfo}"
                            )

                            Log.e(
                                "AppLockAdMob",
                                "================================"
                            )

                            analytics.logEvent(
                                "interstitial_ad_failed"
                            ) {

                                param(
                                    "ad_type",
                                    "interstitial"
                                )

                                param(
                                    "ad_unit_id",
                                    interstitialAdUnitId
                                )

                                param(
                                    "error_code",
                                    loadAdError.code.toString()
                                )

                                param(
                                    "error_message",
                                    loadAdError.message
                                )

                                param(
                                    "screen",
                                    "welcome_screen"
                                )
                            }

                            /*
                             * If the ad cannot load,
                             * continue to the next screen.
                             */
                            goToLanguages()
                        }
                    }
                )
            }

            /*
             * =====================================================
             * SAFETY TIMEOUT
             * =====================================================
             */

            delay(20_000)

            if (!navigationDone) {

                Log.e(
                    "AppLockAdMob",
                    "AD LOAD TIMEOUT"
                )

                analytics.logEvent(
                    "interstitial_ad_timeout"
                ) {

                    param(
                        "ad_type",
                        "interstitial"
                    )

                    param(
                        "ad_unit_id",
                        interstitialAdUnitId
                    )

                    param(
                        "timeout_seconds",
                        "20"
                    )

                    param(
                        "screen",
                        "welcome_screen"
                    )
                }

                goToLanguages()
            }
        }

        /*
         * =========================================================
         * LOADING SCREEN
         * =========================================================
         */

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                CircularProgressIndicator(
                    modifier = Modifier.size(42.dp),
                    color = Color(0xFF2196F3),
                    strokeWidth = 4.dp
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "Test Ad is Loading...",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Spacer(
                    modifier = Modifier.height(7.dp)
                )

                Text(
                    text = "Please wait",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

    } else {

        /*
         * =========================================================
         * WELCOME SCREEN
         * =========================================================
         */

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
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp),
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
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = buildAnnotatedString {

                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF333333),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("App ")
                        }

                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Bold
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
                    text = "Secure your apps. Protect your privacy.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = {

                    if (!isLoadingAd) {
                        isLoadingAd = true
                    }

                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.navigationBars
                    )
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        bottom = 18.dp
                    )
                    .height(51.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {

                Text(
                    text = "Get Started",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

