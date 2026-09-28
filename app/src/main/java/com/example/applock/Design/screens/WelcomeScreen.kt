
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.viewinterop.AndroidView

import androidx.navigation.NavController

import com.example.applock.R

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

import kotlinx.coroutines.delay


private const val TAG = "AppLockAdMob"

private const val TEST_INTERSTITIAL_AD_UNIT_ID =
    "ca-app-pub-3940256099942544/1033173712"

private const val TEST_BANNER_AD_UNIT_ID =
    "ca-app-pub-3940256099942544/9214589741"

private const val TEST_DEVICE_ID =
    "E5CA263188C38AF6BD96C4C84E9600BB"


@Composable
private fun AdaptiveBannerAd(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val adView = remember {
        AdView(context)
    }

    DisposableEffect(adView) {

        adView.adUnitId =
            TEST_BANNER_AD_UNIT_ID

        val displayMetrics =
            context.resources.displayMetrics

        val screenWidthDp =
            (
                    displayMetrics.widthPixels /
                            displayMetrics.density
                    ).toInt()

        val adSize =
            AdSize.getLargeAnchoredAdaptiveBannerAdSize(
                context,
                screenWidthDp
            )

        adView.setAdSize(adSize)

        adView.adListener =
            object : AdListener() {

                override fun onAdLoaded() {

                    Log.d(
                        TAG,
                        "================================"
                    )

                    Log.d(
                        TAG,
                        "ADAPTIVE BANNER LOADED"
                    )

                    Log.d(
                        TAG,
                        "Banner Ad Unit ID = $TEST_BANNER_AD_UNIT_ID"
                    )

                    Log.d(
                        TAG,
                        "Banner Size = ${adView.adSize}"
                    )

                    Log.d(
                        TAG,
                        "================================"
                    )
                }

                override fun onAdFailedToLoad(
                    adError: LoadAdError
                ) {

                    Log.e(
                        TAG,
                        "================================"
                    )

                    Log.e(
                        TAG,
                        "ADAPTIVE BANNER FAILED"
                    )

                    Log.e(
                        TAG,
                        "Banner Ad Unit ID = $TEST_BANNER_AD_UNIT_ID"
                    )

                    Log.e(
                        TAG,
                        "Error Code = ${adError.code}"
                    )

                    Log.e(
                        TAG,
                        "Error Message = ${adError.message}"
                    )

                    Log.e(
                        TAG,
                        "Error Domain = ${adError.domain}"
                    )

                    Log.e(
                        TAG,
                        "Response Info = ${adError.responseInfo}"
                    )

                    Log.e(
                        TAG,
                        "================================"
                    )
                }

                override fun onAdOpened() {
                    Log.d(
                        TAG,
                        "Adaptive banner opened"
                    )
                }

                override fun onAdClosed() {
                    Log.d(
                        TAG,
                        "Adaptive banner closed"
                    )
                }

                override fun onAdClicked() {
                    Log.d(
                        TAG,
                        "Adaptive banner clicked"
                    )
                }

                override fun onAdImpression() {
                    Log.d(
                        TAG,
                        "Adaptive banner impression"
                    )
                }
            }

        val adRequest =
            AdRequest.Builder()
                .build()

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "Loading Adaptive Banner..."
        )

        Log.d(
            TAG,
            "Banner Ad Unit ID = $TEST_BANNER_AD_UNIT_ID"
        )

        Log.d(
            TAG,
            "Screen Width DP = $screenWidthDp"
        )

        Log.d(
            TAG,
            "Ad Size = $adSize"
        )

        Log.d(
            TAG,
            "================================"
        )

        adView.loadAd(
            adRequest
        )

        onDispose {

            Log.d(
                TAG,
                "Destroying Adaptive Banner..."
            )

            adView.destroy()
        }
    }

    AndroidView(
        factory = {
            adView
        },
        modifier = modifier
    )
}


@Composable
fun WelcomeScreen(
    navController: NavController
) {

    val context =
        LocalContext.current

    val activity =
        context as? Activity

    val analytics =
        remember {
            FirebaseAnalytics.getInstance(
                context
            )
        }

    var isLoadingAd by remember {
        mutableStateOf(false)
    }

    var navigationDone by remember {
        mutableStateOf(false)
    }


    /*
     * ADMOB INITIALIZATION
     */

    LaunchedEffect(Unit) {

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "WelcomeScreen started"
        )

        try {

            val requestConfiguration =
                RequestConfiguration.Builder()
                    .setTestDeviceIds(
                        listOf(
                            TEST_DEVICE_ID
                        )
                    )
                    .build()

            MobileAds.setRequestConfiguration(
                requestConfiguration
            )

            Log.d(
                TAG,
                "TEST DEVICE CONFIGURED"
            )

            Log.d(
                TAG,
                "Test Device ID = $TEST_DEVICE_ID"
            )

            Log.d(
                TAG,
                "Registered Test Device IDs = ${
                    MobileAds
                        .getRequestConfiguration()
                        .testDeviceIds
                }"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Failed to configure test device",
                e
            )
        }

        Log.d(
            TAG,
            "Initializing Google Mobile Ads SDK..."
        )

        MobileAds.initialize(context) {

            Log.d(
                TAG,
                "AdMob initialized successfully"
            )

            Log.d(
                TAG,
                "Ad Inspector should now be available"
            )

            Log.d(
                TAG,
                "================================"
            )
        }
    }


    /*
     * NAVIGATION
     */

    fun goToLanguages() {

        if (navigationDone) {
            return
        }

        navigationDone = true

        Log.d(
            TAG,
            "Navigating to languagesSetup"
        )

        navController.navigate(
            "languagesSetup"
        ) {

            popUpTo(
                "welcomeScreen"
            ) {
                inclusive = true
            }

            launchSingleTop = true
        }
    }


    /*
     * INTERSTITIAL
     */

    if (isLoadingAd) {

        BackHandler {

        }

        LaunchedEffect(Unit) {

            Log.d(
                TAG,
                "Get Started -> Starting ad flow"
            )

            analytics.logEvent(
                "interstitial_ad_loading"
            ) {

                param(
                    "ad_type",
                    "interstitial"
                )

                param(
                    "screen",
                    "welcome_screen"
                )
            }

            val adRequest =
                AdRequest.Builder()
                    .build()

            Log.d(
                TAG,
                "Loading official Google test interstitial..."
            )

            Log.d(
                TAG,
                "Ad Unit ID = $TEST_INTERSTITIAL_AD_UNIT_ID"
            )

            InterstitialAd.load(
                context,
                TEST_INTERSTITIAL_AD_UNIT_ID,
                adRequest,

                object :
                    InterstitialAdLoadCallback() {

                    override fun onAdLoaded(
                        interstitialAd: InterstitialAd
                    ) {

                        if (navigationDone) {
                            return
                        }

                        Log.d(
                            TAG,
                            "================================"
                        )

                        Log.d(
                            TAG,
                            "INTERSTITIAL AD LOADED SUCCESSFULLY"
                        )

                        Log.d(
                            TAG,
                            "================================"
                        )

                        analytics.logEvent(
                            "interstitial_ad_loaded"
                        ) {

                            param(
                                "ad_type",
                                "interstitial"
                            )

                            param(
                                "screen",
                                "welcome_screen"
                            )
                        }

                        interstitialAd.fullScreenContentCallback =
                            object :
                                FullScreenContentCallback() {

                                override fun onAdShowedFullScreenContent() {

                                    Log.d(
                                        TAG,
                                        "INTERSTITIAL AD SHOWN"
                                    )

                                    analytics.logEvent(
                                        "interstitial_ad_shown"
                                    ) {

                                        param(
                                            "ad_type",
                                            "interstitial"
                                        )

                                        param(
                                            "screen",
                                            "welcome_screen"
                                        )
                                    }
                                }

                                override fun onAdDismissedFullScreenContent() {

                                    Log.d(
                                        TAG,
                                        "INTERSTITIAL AD CLOSED"
                                    )

                                    analytics.logEvent(
                                        "interstitial_ad_closed"
                                    ) {

                                        param(
                                            "ad_type",
                                            "interstitial"
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
                                        TAG,
                                        "INTERSTITIAL AD FAILED TO SHOW"
                                    )

                                    Log.e(
                                        TAG,
                                        "Show Code = ${adError.code}"
                                    )

                                    Log.e(
                                        TAG,
                                        "Show Message = ${adError.message}"
                                    )

                                    Log.e(
                                        TAG,
                                        "Show Domain = ${adError.domain}"
                                    )

                                    analytics.logEvent(
                                        "interstitial_ad_show_failed"
                                    ) {

                                        param(
                                            "ad_type",
                                            "interstitial"
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

                        if (
                            activity != null &&
                            !activity.isFinishing &&
                            !activity.isDestroyed
                        ) {

                            try {

                                Log.d(
                                    TAG,
                                    "Showing interstitial ad..."
                                )

                                interstitialAd.show(
                                    activity
                                )

                            } catch (e: Exception) {

                                Log.e(
                                    TAG,
                                    "Exception while showing interstitial",
                                    e
                                )

                                goToLanguages()
                            }

                        } else {

                            Log.e(
                                TAG,
                                "Activity is not available"
                            )

                            goToLanguages()
                        }
                    }

                    override fun onAdFailedToLoad(
                        loadAdError: LoadAdError
                    ) {

                        Log.e(
                            TAG,
                            "================================"
                        )

                        Log.e(
                            TAG,
                            "INTERSTITIAL AD FAILED TO LOAD"
                        )

                        Log.e(
                            TAG,
                            "Code = ${loadAdError.code}"
                        )

                        Log.e(
                            TAG,
                            "Message = ${loadAdError.message}"
                        )

                        Log.e(
                            TAG,
                            "Domain = ${loadAdError.domain}"
                        )

                        Log.e(
                            TAG,
                            "ResponseInfo = ${loadAdError.responseInfo}"
                        )

                        Log.e(
                            TAG,
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

                        goToLanguages()
                    }
                }
            )

            delay(20_000)

            if (!navigationDone) {

                Log.e(
                    TAG,
                    "INTERSTITIAL AD LOAD TIMEOUT"
                )

                analytics.logEvent(
                    "interstitial_ad_timeout"
                ) {

                    param(
                        "ad_type",
                        "interstitial"
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
         * LOADING UI
         */

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.White),

            contentAlignment =
                Alignment.Center
        ) {

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center
            ) {

                CircularProgressIndicator(
                    modifier =
                        Modifier.size(42.dp),

                    color =
                        Color(0xFF2196F3),

                    strokeWidth =
                        4.dp
                )

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Text(
                    text =
                        "Test Ad is Loading...",

                    fontSize =
                        17.sp,

                    fontWeight =
                        FontWeight.Medium,

                    color =
                        Color(0xFF333333)
                )



            }
        }

    } else {

        /*
         * NORMAL WELCOME SCREEN
         */

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
        ) {

            /*
             * LOGO + TITLE
             */

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(
                            horizontal = 20.dp
                        ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Image(
                    painter =
                        painterResource(
                            id =
                                R.drawable.startscreen
                        ),

                    contentDescription =
                        "App Lock",

                    modifier =
                        Modifier.size(270.dp)
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
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

                                append(
                                    "App "
                                )
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

                                append(
                                    "Lock"
                                )
                            }
                        },

                    fontSize =
                        34.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
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


            /*
             * =================================================
             * AD BANNER
             *
             * Banner is now directly above
             * Ad Inspector / Get Started area.
             * =================================================
             */

            Box(
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .fillMaxWidth()
                        .padding(
                            start = 8.dp,
                            end = 8.dp,
                            bottom = 151.dp
                        )
            ) {

                AdaptiveBannerAd(
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }


            /*
             * =================================================
             * AD INSPECTOR
             * =================================================
             */

            Button(
                onClick = {

                    Log.d(
                        TAG,
                        "Opening Ad Inspector..."
                    )

                    MobileAds.openAdInspector(
                        context
                    ) { error ->

                        if (error != null) {

                            Log.e(
                                TAG,
                                "Ad Inspector failed"
                            )

                            Log.e(
                                TAG,
                                "Inspector Error = ${error.message}"
                            )

                        } else {

                            Log.d(
                                TAG,
                                "Ad Inspector closed successfully"
                            )
                        }
                    }
                },

                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.navigationBars
                        )
                        .padding(
                            start = 14.dp,
                            end = 14.dp,
                            bottom = 80.dp
                        )
                        .height(45.dp),

                shape =
                    RoundedCornerShape(12.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF555555)
                    )
            ) {

                Text(
                    text =
                        "Open Ad Inspector",

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.Medium,

                    color =
                        Color.White
                )
            }


            /*
             * =================================================
             * GET STARTED
             * =================================================
             */

            Button(
                onClick = {

                    if (!isLoadingAd) {

                        Log.d(
                            TAG,
                            "Get Started clicked"
                        )

                        isLoadingAd = true
                    }
                },

                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
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

                shape =
                    RoundedCornerShape(14.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF2196F3)
                    )
            ) {

                Text(
                    text =
                        "Get Started",

                    fontSize =
                        17.sp,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        Color.White
                )
            }
        }
    }
}

