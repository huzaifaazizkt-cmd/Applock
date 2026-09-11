package com.example.applock.navigation

import android.content.Context
import android.net.Uri

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.applock.Design.screens.ExitScreen
import com.example.applock.Design.screens.HomeScreen
import com.example.applock.Design.screens.IntruderScreen
import com.example.applock.Design.screens.LanguagesScreen
import com.example.applock.Design.screens.MainScreen
import com.example.applock.Design.screens.OnboardingScreen
import com.example.applock.Design.screens.PinConfirmScreen
import com.example.applock.Design.screens.PinCreateScreen
import com.example.applock.Design.screens.PremiumScreen
import com.example.applock.Design.screens.StartScreen
import com.example.applock.Design.screens.WelcomeScreen

import com.example.applock.data.DataStoreManager


@Composable
fun NavGraph(
    context: Context
) {

    val appContext =
        context.applicationContext


    val dataStore =
        DataStoreManager(
            appContext
        )


    val appInitialized by
    dataStore
        .getAppInitialized()
        .collectAsState(
            initial = null
        )


    if (
        appInitialized == null
    ) {

        return
    }


    val navController =
        rememberNavController()


    NavHost(

        navController =
            navController,

        startDestination =
            "startScreen"
    ) {


        // =====================================================
        // START SCREEN
        // =====================================================

        composable(
            "startScreen"
        ) {

            StartScreen(

                navController =
                    navController,

                appInitialized =
                    appInitialized == true
            )
        }


        // =====================================================
        // WELCOME
        // =====================================================

        composable(
            "welcomeScreen"
        ) {

            WelcomeScreen(
                navController =
                    navController
            )
        }


        // =====================================================
        // LANGUAGE SETUP
        // =====================================================

        composable(
            "languagesSetup"
        ) {

            LanguagesScreen(

                onBackClick =
                    null,

                onLanguageSelected = {

                    navController.navigate(
                        "onboardingScreen"
                    ) {

                        popUpTo(
                            "languagesSetup"
                        ) {

                            inclusive =
                                true
                        }
                    }
                }
            )
        }


        // =====================================================
        // ONBOARDING
        // =====================================================

        composable(
            "onboardingScreen"
        ) {

            OnboardingScreen(
                navController =
                    navController
            )
        }


        // =====================================================
        // PREMIUM
        // =====================================================

        composable(
            "premium"
        ) {

            PremiumScreen(
                navController =
                    navController
            )
        }


        // =====================================================
        // CREATE PASSWORD
        // =====================================================

        composable(
            "create"
        ) {

            PinCreateScreen(

                onNext = {
                        type,
                        value ->

                    navController.navigate(

                        "confirm/${
                            Uri.encode(type)
                        }/${
                            Uri.encode(value)
                        }"
                    )
                }
            )
        }


        // =====================================================
        // CONFIRM PASSWORD
        // =====================================================

        composable(

            route =
                "confirm/{type}/{value}",

            arguments =
                listOf(

                    navArgument(
                        "type"
                    ) {

                        type =
                            NavType.StringType
                    },

                    navArgument(
                        "value"
                    ) {

                        type =
                            NavType.StringType
                    }
                )
        ) {

            val type =
                it.arguments
                    ?.getString(
                        "type"
                    )
                    ?: "pin"


            val value =
                Uri.decode(

                    it.arguments
                        ?.getString(
                            "value"
                        )
                        ?: ""
                )


            PinConfirmScreen(

                navController =
                    navController,

                context =
                    context,

                type =
                    type,

                value =
                    value,

                isReset =
                    false
            )
        }


        // =====================================================
        // RESET PASSWORD CREATE
        // =====================================================

        composable(
            "resetCreate"
        ) {

            PinCreateScreen(

                onNext = {
                        type,
                        value ->

                    navController.navigate(

                        "resetConfirm/${
                            Uri.encode(type)
                        }/${
                            Uri.encode(value)
                        }"
                    )
                }
            )
        }


        // =====================================================
        // RESET PASSWORD CONFIRM
        // =====================================================

        composable(

            route =
                "resetConfirm/{type}/{value}",

            arguments =
                listOf(

                    navArgument(
                        "type"
                    ) {

                        type =
                            NavType.StringType
                    },

                    navArgument(
                        "value"
                    ) {

                        type =
                            NavType.StringType
                    }
                )
        ) {

            val type =
                it.arguments
                    ?.getString(
                        "type"
                    )
                    ?: "pin"


            val value =
                Uri.decode(

                    it.arguments
                        ?.getString(
                            "value"
                        )
                        ?: ""
                )


            PinConfirmScreen(

                navController =
                    navController,

                context =
                    context,

                type =
                    type,

                value =
                    value,

                isReset =
                    true
            )
        }


        // =====================================================
        // HOME
        // =====================================================

        composable(
            "home"
        ) {

            HomeScreen(
                navController =
                    navController
            )
        }


        // =====================================================
        // MAIN APP
        // =====================================================

        composable(
            "appList"
        ) {

            val currentEntry =
                navController
                    .currentBackStackEntry


            val openSettings =
                currentEntry
                    ?.savedStateHandle
                    ?.get<Boolean>(
                        "openSettings"
                    )
                    ?: false


            MainScreen(

                context =
                    context,

                // =================================================
                // SETTINGS -> INTRUDER
                // =================================================

                onIntruderClick = {

                    // MainScreen ke savedStateHandle mein
                    // mark karo ke wapas Settings par jana hai.

                    currentEntry
                        ?.savedStateHandle
                        ?.set(
                            "returnToSettings",
                            true
                        )


                    navController.navigate(
                        "intruder"
                    )
                },


                // =================================================
                // SETTINGS -> LANGUAGES
                // =================================================

                onLanguageClick = {

                    // MainScreen ko bata do ke
                    // Languages se back hone par
                    // Settings tab select karna hai.

                    currentEntry
                        ?.savedStateHandle
                        ?.set(
                            "returnToSettings",
                            true
                        )


                    navController.navigate(
                        "languages"
                    )
                },


                // =================================================
                // SETTINGS -> RESET PASSWORD
                // =================================================

                onResetPasswordClick = {

                    navController.navigate(
                        "resetCreate"
                    )
                },


                openSettings =
                    openSettings,

                navController =
                    navController
            )
        }


        // =====================================================
        // LANGUAGES
        // =====================================================

        composable(
            "languages"
        ) {

            LanguagesScreen(

                onBackClick = {

                    navController
                        .popBackStack()
                },

                onLanguageSelected =
                    null
            )
        }


        // =====================================================
        // INTRUDER
        // =====================================================

        composable(
            "intruder"
        ) {

            IntruderScreen(

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }


        // =====================================================
        // EXIT
        // =====================================================

        composable(
            "exit"
        ) {

            ExitScreen(
                navController =
                    navController
            )
        }
    }
}