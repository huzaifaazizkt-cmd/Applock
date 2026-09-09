package com.example.applock.navigation

import android.content.Context

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

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

    val navController =
        rememberNavController()

    val appContext =
        remember {
            context.applicationContext
        }

    val dataStore =
        remember {
            DataStoreManager(
                appContext
            )
        }

    /*
     * null  = DataStore abhi load ho raha hai
     * false = first launch
     * true  = setup complete
     */
    val appInitialized by
    dataStore
        .getAppInitialized()
        .collectAsState(
            initial = null
        )

    /*
     * Jab tak DataStore ka result nahi aata,
     * NavHost create nahi hoga.
     */
    if (appInitialized == null) {
        return
    }

    val startDestination =
        if (appInitialized == true) {
            "appList"
        } else {
            "startScreen"
        }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // =================================================
        // START
        // =================================================

        composable("startScreen") {

            StartScreen(
                navController = navController
            )
        }

        // =================================================
        // WELCOME
        // =================================================

        composable("welcomeScreen") {

            WelcomeScreen(
                navController = navController
            )
        }

        // =================================================
        // LANGUAGE SETUP
        // =================================================

        composable("languagesSetup") {

            LanguagesScreen(

                onBackClick = null,

                onLanguageSelected = {

                    navController.navigate(
                        "onboardingScreen"
                    ) {

                        popUpTo(
                            "languagesSetup"
                        ) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // =================================================
        // ONBOARDING
        // =================================================

        composable("onboardingScreen") {

            OnboardingScreen(
                navController = navController
            )
        }

        // =================================================
        // PREMIUM
        // =================================================

        composable("premium") {

            PremiumScreen(
                navController = navController
            )
        }

        // =================================================
        // PIN / PATTERN CREATE
        // =================================================

        composable("create") {

            PinCreateScreen(

                onNext = { type, value ->

                    when (type) {

                        "pin" -> {

                            navController.navigate(
                                "confirm/pin/$value"
                            )
                        }

                        "pattern" -> {

                            navController.navigate(
                                "confirm/pattern/$value"
                            )
                        }
                    }
                }
            )
        }

        // =================================================
        // PIN / PATTERN CONFIRM
        // =================================================

        composable(
            route = "confirm/{type}/{value}",

            arguments = listOf(

                navArgument("type") {
                    type = NavType.StringType
                },

                navArgument("value") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val type =
                backStackEntry.arguments
                    ?.getString("type")
                    ?: "pin"

            val value =
                backStackEntry.arguments
                    ?.getString("value")
                    ?: ""

            PinConfirmScreen(

                navController = navController,

                context = context,

                type = type,

                value = value
            )
        }

        // =================================================
        // HOME
        // =================================================

        composable("home") {

            HomeScreen(
                navController
            )
        }

        // =================================================
        // APP LIST / MAIN
        // =================================================

        composable("appList") {

            val openSettings =
                navController
                    .currentBackStackEntry
                    ?.savedStateHandle
                    ?.get<Boolean>(
                        "openSettings"
                    )
                    ?: false

            MainScreen(

                context = context,

                onIntruderClick = {

                    navController.navigate(
                        "intruder"
                    )
                },

                onLanguageClick = {

                    navController.navigate(
                        "languages"
                    )
                },

                openSettings = openSettings,

                navController = navController
            )
        }

        // =================================================
        // LANGUAGES
        // =================================================

        composable("languages") {

            LanguagesScreen(

                onBackClick = {

                    navController
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            "openSettings",
                            true
                        )

                    navController.popBackStack()
                },

                onLanguageSelected = null
            )
        }

        // =================================================
        // INTRUDER
        // =================================================

        composable("intruder") {

            IntruderScreen(

                onBackClick = {

                    navController
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            "openSettings",
                            true
                        )

                    navController.popBackStack()
                }
            )
        }

        // =================================================
        // EXIT
        // =================================================

        composable("exit") {

            ExitScreen(
                navController = navController
            )
        }
    }
}