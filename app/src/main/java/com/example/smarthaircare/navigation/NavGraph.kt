package com.example.smarthaircare.navigation

import HomeScreen
import OnboardingScreen
import ProfileScreen
import ResultsScreen
import ScanScreen
import SplashScreen
import UserSelections
import android.net.Uri
import android.os.Bundle
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smarthaircare.ui.components.BottomNavigationBar
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val bottomNavScreens = listOf(Screen.Home, Screen.Scan, Screen.Profile)
    val showBottomNav = currentRoute?.let { route ->
        bottomNavScreens.any { screen -> route.startsWith(screen.route) }
    } ?: false

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToScan = {
                        navController.navigate(Screen.Scan.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }

            composable(Screen.Scan.route) {
                ScanScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    // Instead of the complex navigation
                    onNavigateToResults = { imageUri, scanResponse, userSelections ->
                        ScanDataHolder.currentScanResponse = scanResponse
                        ScanDataHolder.currentImageUri = imageUri
                        ScanDataHolder.currentUserSelections = userSelections

                        navController.navigate(Screen.Results.route)
                    }
                )
            }

            composable(Screen.Results.route) {
                ResultsScreen(
                    imageUri = ScanDataHolder.currentImageUri ?: Uri.EMPTY,
                    scanResponse = ScanDataHolder.currentScanResponse,
                    userSelections = ScanDataHolder.currentUserSelections ?: UserSelections(),
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}