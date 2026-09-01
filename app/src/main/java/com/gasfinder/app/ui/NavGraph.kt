package com.gasfinder.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gasfinder.app.network.TokenManager

private const val ROUTE_LOGIN = "login"
private const val ROUTE_HOME = "home"
private const val ROUTE_DETAIL = "detail/{retailerId}"
private const val ROUTE_REGISTER = "register"
private const val ROUTE_ADMIN = "admin"

@Composable
fun GasFinderNavGraph() {
    val navController: NavHostController = rememberNavController()
    val startDestination = if (TokenManager.isLoggedIn()) ROUTE_HOME else ROUTE_LOGIN

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ROUTE_LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(ROUTE_REGISTER)
                }
            )
        }
        composable(ROUTE_HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_HOME) { inclusive = true }
                    }
                },
                onRetailerClick = { retailerId ->
                    navController.navigate("detail/$retailerId")
                },
                onAdminClick = {
                    navController.navigate(ROUTE_ADMIN)
                }
            )
        }
        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(navArgument("retailerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val retailerId = backStackEntry.arguments?.getString("retailerId") ?: ""
            RetailerDetailScreen(
                retailerId = retailerId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(ROUTE_REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(ROUTE_ADMIN) {
            AdminScreen(onBack = { navController.popBackStack() })
        }
    }
}
