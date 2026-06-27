package com.example.ekiwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
        modifier = modifier.shadow(30.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant

    ) {
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.navbar_favorite), contentDescription = "Favorite Locations") },
            label = { Text("Favorites") },
            selected = currentRoute?.hierarchy?.any { it.route == EkiWatchDestination.Favorites.route } == true,
            onClick = { navigateToTab(EkiWatchDestination.Favorites.route) }
        )
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.navbar_home), contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute?.hierarchy?.any { it.route == EkiWatchDestination.Home.route } == true,
            onClick = { navigateToTab(EkiWatchDestination.Home.route) }
        )
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.navbar_calendar_clock), contentDescription = "Past Locations") },
            label = { Text("Visited") },
            selected = currentRoute?.hierarchy?.any { it.route == EkiWatchDestination.Visited.route } == true,
            onClick = { navigateToTab(EkiWatchDestination.Visited.route) }
        )
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.navbar_settings_cog), contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute?.hierarchy?.any { it.route == EkiWatchDestination.Settings.route } == true,
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