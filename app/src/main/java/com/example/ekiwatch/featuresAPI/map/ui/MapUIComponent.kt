package com.example.ekiwatch.featuresAPI.map.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ekiwatch.featuresAPI.map.viewModel.MapViewModel
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Polyline

@Composable
fun MapUIComponent(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current

    // Check both permissions on startup
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        hasNotificationPermission = isGranted
        if (isGranted) viewModel.loadUserLocation()
    }

    // Check all the necessary requirements for the app upon startup
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else if (!hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.loadUserLocation()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = viewModel.cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {

            // Walking lines
            if (viewModel.walkingPolylinePoints.isNotEmpty()) {
                Polyline(
                    points = viewModel.walkingPolylinePoints,
                    color = Color.Blue,
                    width = 15f
                )
            }

            // Train lines
            if (viewModel.routingRepository.trainPolylinePoints.isNotEmpty()) {
                Polyline(
                    points = viewModel.routingRepository.trainPolylinePoints,
                    clickable = false,
                    color = Color(0xFFFF9800), // Train line marker color
                    width = 14f,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    jointType = JointType.ROUND
                )
            }
        }
        SearchComponent(
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchComponent(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    // 1. Use rememberSaveable to keep active/text state alive
    var active by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }

    SearchBar(
        modifier = modifier,
        query = text,
        onQueryChange = { newText ->
            text = newText // Update local UI immediately
            viewModel.onSearchQueryChanged(newText) // Async fetch in background, see MapViewModel
        },
        onSearch = { active = false },
        active = active,
        onActiveChange = { active = it },
        placeholder = { Text("Where ya off too?") }
    ) {
        // The Lazy Column that displays the result from the search that is above
        LazyColumn {
            items(viewModel.searchResults) { place ->
                ListItem(
                    headlineContent = { Text(place.getPrimaryText(null).toString()) },
                    modifier = Modifier.clickable {
                        active = false
                        text = place.getPrimaryText(null).toString()

                        viewModel.resolveAndSelectDestination(place.placeId)
                    }
                )
            }
        }
    }
}