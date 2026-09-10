package com.example.applock.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
        DataStoreManager(appContext)

    val appInitialized by
    dataStore
        .getAppInitialized()
        .collectAsState(initial = null)

    if (appInitialized == null) {
        return
    }

    val navController =
        rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "startScreen"
    ) {

        composable("startScreen") {

            StartScreen(
                navController = navController,
                appInitialized =
                    appInitialized == true
            )
        }

        composable("welcomeScreen") {

            WelcomeScreen(
                navController = navController
            )
        }

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

        composable("onboardingScreen") {

            OnboardingScreen(
                navController = navController
            )
        }

        composable("premium") {

            PremiumScreen(
                navController = navController
            )
        }

        // =====================================================
        // FIRST TIME CREATE PASSWORD
        // =====================================================

        composable("create") {

            PinCreateScreen(
                onNext = { type, value ->

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
        // FIRST TIME CONFIRM PASSWORD
        // =====================================================

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
                backStackEntry
                    .arguments
                    ?.getString("type")
                    ?: "pin"

            val value =
                Uri.decode(
                    backStackEntry
                        .arguments
                        ?.getString("value")
                        ?: ""
                )

            PinConfirmScreen(
                navController = navController,
                context = context,
                type = type,
                value = value,
                isReset = false
            )
        }

        // =====================================================
        // RESET PASSWORD CREATE
        // =====================================================

        composable("resetCreate") {

            PinCreateScreen(
                onNext = { type, value ->

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
            route = "resetConfirm/{type}/{value}",

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
                backStackEntry
                    .arguments
                    ?.getString("type")
                    ?: "pin"

            val value =
                Uri.decode(
                    backStackEntry
                        .arguments
                        ?.getString("value")
                        ?: ""
                )

            PinConfirmScreen(
                navController = navController,
                context = context,
                type = type,
                value = value,
                isReset = true
            )
        }

        composable("home") {

            HomeScreen(
                navController = navController
            )
        }

        composable("appList") {

            val openSettings =
                navController
                    .currentBackStackEntry
                    ?.savedStateHandle
                    ?.get<Boolean>("openSettings")
                    ?: false

            MainScreen(
                context = context,

                onIntruderClick = {
                    navController.navigate("intruder")
                },

                onLanguageClick = {
                    navController.navigate("languages")
                },

                openSettings = openSettings,

                navController = navController
            )
        }

        composable("languages") {

            LanguagesScreen(
                onBackClick = {
                    navController.popBackStack()
                },

                onLanguageSelected = null
            )
        }

        composable("intruder") {

            IntruderScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("exit") {

            ExitScreen(
                navController = navController
            )
        }
    }
}