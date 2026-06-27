package com.example.ekiwatch.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ekiwatch.featuresAPI.favorites.ui.FavoritesUI
import com.example.ekiwatch.featuresAPI.map.ui.MapUIComponent
import com.example.ekiwatch.featuresAPI.recent.ui.RecentPlacesUI
import com.example.ekiwatch.featuresAPI.settings.ui.SettingsUI

@Composable
fun EkiWatchNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = EkiWatchDestination.Home.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(EkiWatchDestination.Home.route) {
            MapUIComponent()
        }
        composable(EkiWatchDestination.Favorites.route) {
            FavoritesUI()
        }
        composable(EkiWatchDestination.Visited.route) {
            RecentPlacesUI()
        }
        composable(EkiWatchDestination.Settings.route) {
            SettingsUI()
        }
    }
}