
package com.example.applock.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.applock.Design.screens.HomeScreen
import com.example.applock.Design.screens.IntruderScreen
import com.example.applock.Design.screens.MainScreen
import com.example.applock.Design.screens.PinConfirmScreen
import com.example.applock.Design.screens.PinCreateScreen

@Composable
fun NavGraph(
    context: Context
) {

    val navController =
        rememberNavController()


    NavHost(
        navController = navController,
        startDestination = "create"
    ) {


        // =====================================================
        // CREATE
        // =====================================================

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


        // =====================================================
        // CONFIRM
        // =====================================================

        composable(
            route = "confirm/{type}/{value}",

            arguments = listOf(

                navArgument("type") {

                    type =
                        NavType.StringType
                },

                navArgument("value") {

                    type =
                        NavType.StringType
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

                navController =
                    navController,

                context =
                    context,

                type =
                    type,

                value =
                    value
            )
        }


        // =====================================================
        // HOME
        // =====================================================

        composable("home") {

            HomeScreen(
                navController
            )
        }


        // =====================================================
        // APP LIST / MAIN SCREEN
        // =====================================================

        composable("appList") {

            val openSettings =
                navController
                    .currentBackStackEntry
                    ?.savedStateHandle
                    ?.get<Boolean>("openSettings")
                    ?: false


            MainScreen(

                context =
                    context,

                // =================================================
                // SETTINGS -> INTRUDER
                // =================================================

                onIntruderClick = {

                    navController.navigate(
                        "intruder"
                    )
                },

                // =================================================
                // DEFAULT
                // =================================================

                openSettings =
                    openSettings
            )
        }


        // =====================================================
        // INTRUDER SCREEN
        // =====================================================

        composable("intruder") {

            IntruderScreen(

                onBackClick = {

                    // =================================================
                    // MAIN SCREEN KO BATAYEIN:
                    // SETTINGS TAB OPEN KARNA HAI
                    // =================================================

                    navController
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            "openSettings",
                            true
                        )

                    // =================================================
                    // INTRUDER SE MAIN SCREEN PAR WAPAS
                    // =================================================

                    navController.popBackStack()
                }
            )
        }
    }
}

