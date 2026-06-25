package com.example.ekiwatch

import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ekiwatch.featuresAPI.map.ui.MapViewComponent
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
    Scaffold(
        bottomBar = { BottomBar() }
    ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            )
            {
                MapViewComponent()
            }
    }

}

@Composable
fun BottomBar(modifier: Modifier = Modifier)
{

    val context = LocalContext.current

    NavigationBar(
        modifier = modifier.shadow(30.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant

    ) {
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.navbar_favorite), contentDescription = "Favorite Locations") },
            label = { Text("Favorites") },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.navbar_home), contentDescription = "Home") },
            label = { Text("Home") },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.navbar_calendar_clock), contentDescription = "Past Locations") },
            label = { Text("Visited") },
            selected = false,
            onClick = {  }
        )
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.navbar_settings_cog), contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = false,
            onClick = {  }
        )
    }
}

@Preview
@Composable
fun ApplicationPreview(modifier: Modifier = Modifier)
{
    Application()
}
