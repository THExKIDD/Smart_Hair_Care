package com.example.smarthaircare.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Scan : Screen("scan")
    object Results : Screen("results")
    object Profile : Screen("profile")
}
