package com.example.applock.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.applock.Design.screens.*

@Composable
fun NavGraph(context: Context) {

    val navController = rememberNavController()

    NavHost(navController, startDestination = "create") {

        composable("create") {
            PinCreateScreen {
                navController.navigate("confirm/$it")
            }
        }

        composable("confirm/{pin}") {
            val pin = it.arguments?.getString("pin") ?: ""
            PinConfirmScreen(navController, context, pin)
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable("appList") {
            AppListScreen(context)
        }
    }
}