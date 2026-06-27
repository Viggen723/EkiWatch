package com.example.ekiwatch.navigation

/**
 * The four top-level destinations, one per BottomBar tab in MainActivity.
 * route is the literal string NavHost/NavController use; keeping them here
 * means the nav graph and the bottom bar can both reference the same
 * constants instead of typing route strings inline in two places.
 */
sealed class EkiWatchDestination(val route: String) {
    object Home : EkiWatchDestination("home")
    object Favorites : EkiWatchDestination("favorites")
    object Visited : EkiWatchDestination("visited")
    object Settings : EkiWatchDestination("settings")
}