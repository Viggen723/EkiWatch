package com.example.ekiwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ekiwatch.featuresAPI.map.ui.BgDeep
import com.example.ekiwatch.featuresAPI.map.ui.Gold
import com.example.ekiwatch.featuresAPI.map.ui.InkFaint
import com.example.ekiwatch.featuresAPI.map.ui.LineColor
import com.example.ekiwatch.featuresAPI.map.ui.Surface
import com.example.ekiwatch.featuresAPI.map.ui.Teal
import com.example.ekiwatch.navigation.EkiWatchDestination
import com.example.ekiwatch.navigation.EkiWatchNavGraph
import com.example.ekiwatch.ui.theme.EkiWatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EkiWatchTheme {
                Application()
            }
        }
    }
}

@Composable
fun Application(modifier: Modifier = Modifier.fillMaxSize())
{
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        )
        {
            EkiWatchNavGraph(navController = navController)

            Text(
                text = "EkiWatch",
                style = TextStyle.Default,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
        }
    }

}

@Composable
fun BottomBar(navController: NavHostController, modifier: Modifier = Modifier)
{
    // Tracks which destination is on top of the back stack so the right
    // tab can be highlighted as selected, including after system back.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            // Pop back to the graph's start destination to avoid stacking
            // up duplicate copies of a tab as the user bounces between them.
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(18.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(BgDeep.copy(alpha = 0.94f))
            .border(1.dp, LineColor.copy(alpha = 0.85f), RoundedCornerShape(26.dp)),
        containerColor = BgDeep.copy(alpha = 0.94f),
        tonalElevation = 0.dp
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Gold,
            selectedTextColor = Gold,
            indicatorColor = Surface.copy(alpha = 0.96f),
            unselectedIconColor = InkFaint,
            unselectedTextColor = InkFaint
        )
        NavigationBarItem(
            icon = {
                Image(
                    painterResource(R.drawable.navbar_favorite),
                    contentDescription = "Favorite Locations",
                    colorFilter = ColorFilter.tint(
                        if (currentRoute?.hierarchy?.any { it.route == EkiWatchDestination.Favorites.route } == true) Gold else InkFaint
                    )
                )
            },
            label = { Text("Favorites") },
            selected = currentRoute?.hierarchy?.any { it.route == EkiWatchDestination.Favorites.route } == true,
            colors = itemColors,
            onClick = { navigateToTab(EkiWatchDestination.Favorites.route) }
        )
        NavigationBarItem(
            icon = {
                Image(
                    painterResource(R.drawable.navbar_home),
                    contentDescription = "Home",
                    colorFilter = ColorFilter.tint(
                        if (currentRoute?.hierarchy?.any { it.route == EkiWatchDestination.Home.route } == true) Teal else InkFaint
                    )
                )
            },
            label = { Text("Home") },
            selected = currentRoute?.hierarchy?.any { it.route == EkiWatchDestination.Home.route } == true,
            colors = itemColors,
            onClick = { navigateToTab(EkiWatchDestination.Home.route) }
        )
        NavigationBarItem(
            icon = {
                Image(
                    painterResource(R.drawable.navbar_calendar_clock),
                    contentDescription = "Past Locations",
                    colorFilter = ColorFilter.tint(
                        if (currentRoute?.hierarchy?.any { it.route == EkiWatchDestination.Visited.route } == true) Gold else InkFaint
                    )
                )
            },
            label = { Text("Visited") },
            selected = currentRoute?.hierarchy?.any { it.route == EkiWatchDestination.Visited.route } == true,
            colors = itemColors,
            onClick = { navigateToTab(EkiWatchDestination.Visited.route) }
        )
        NavigationBarItem(
            icon = {
                Image(
                    painterResource(R.drawable.navbar_settings_cog),
                    contentDescription = "Settings",
                    colorFilter = ColorFilter.tint(
                        if (currentRoute?.hierarchy?.any { it.route == EkiWatchDestination.Settings.route } == true) Gold else InkFaint
                    )
                )
            },
            label = { Text("Settings") },
            selected = currentRoute?.hierarchy?.any { it.route == EkiWatchDestination.Settings.route } == true,
            colors = itemColors,
            onClick = { navigateToTab(EkiWatchDestination.Settings.route) }
        )
    }
}

@Preview
@Composable
fun ApplicationPreview(modifier: Modifier = Modifier)
{
    Application()
}
