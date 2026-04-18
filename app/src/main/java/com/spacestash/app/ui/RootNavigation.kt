package com.spacestash.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RootNavigation() {
    val rootNavController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    val startDest = if (auth.currentUser != null) "main" else "login"

    NavHost(navController = rootNavController, startDestination = startDest) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    rootNavController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { rootNavController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    rootNavController.navigate("main") {
                        popUpTo("register") { inclusive = true }
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = { rootNavController.popBackStack() }
            )
        }

        composable("main") {
            MainScreen()
        }
    }
}