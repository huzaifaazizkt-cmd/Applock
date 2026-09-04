package com.example.applock.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.applock.Design.screens.HomeScreen
import com.example.applock.Design.screens.IntruderScreen
import com.example.applock.Design.screens.LanguagesScreen
import com.example.applock.Design.screens.MainScreen
import com.example.applock.Design.screens.OnboardingScreen
import com.example.applock.Design.screens.PinConfirmScreen
import com.example.applock.Design.screens.PinCreateScreen
import com.example.applock.Design.screens.StartScreen
import com.example.applock.Design.screens.WelcomeScreen


@Composable
fun NavGraph(
    context: Context
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "startScreen"
    ) {



        composable("startScreen") {

            StartScreen(
                navController = navController
            )
        }



        composable("welcomeScreen") {

            WelcomeScreen(
                navController = navController
            )
        }



        composable("onboardingScreen") {

            OnboardingScreen(
                navController = navController
            )
        }




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
                backStackEntry
                    .arguments
                    ?.getString("value")
                    ?: ""

            PinConfirmScreen(

                navController = navController,

                context = context,

                type = type,

                value = value
            )
        }



        composable("home") {

            HomeScreen(
                navController
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

                    navController.navigate(
                        "intruder"
                    )
                },


                onLanguageClick = {

                    navController.navigate(
                        "languages"
                    )
                },


                openSettings = openSettings
            )
        }



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
                }
            )
        }


        // =====================================================
        // INTRUDER SCREEN
        // =====================================================

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
    }
}