package com.example.smarthaircare.navigation

import HomeScreen
import OnboardingScreen
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
import com.example.smarthaircare.ui.screens.AuthScreen
import com.example.smarthaircare.ui.screens.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Check if user is authenticated
    val auth = FirebaseAuth.getInstance()
    val isAuthenticated = auth.currentUser != null

    // Determine start destination based on auth state
    val startDestination = if (isAuthenticated) {
        Screen.Splash.route
    } else {
        Screen.Auth.route
    }

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
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Auth Screen
            composable(Screen.Auth.route) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Screen.Splash.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

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
                    },
                    onSignOut = {
                        // Navigate to auth screen and clear entire back stack
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}