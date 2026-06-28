package com.example.ekiwatch.featuresAPI.map.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var requestedLocationPermission by rememberSaveable { mutableStateOf(false) }
    var requestedNotificationPermission by rememberSaveable { mutableStateOf(false) }
    var locationPermissionFinished by rememberSaveable { mutableStateOf(false) }
    var notificationPermissionFinished by rememberSaveable { mutableStateOf(false) }
    var loadedUserLocation by rememberSaveable { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        locationPermissionFinished = true
        if (isGranted && !loadedUserLocation) {
            loadedUserLocation = true
            viewModel.loadUserLocation()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        notificationPermissionFinished = true
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            locationPermissionFinished = true
            if (!loadedUserLocation) {
                loadedUserLocation = true
                viewModel.loadUserLocation()
            }
        } else if (!requestedLocationPermission) {
            requestedLocationPermission = true
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(locationPermissionFinished) {
        if (!locationPermissionFinished) return@LaunchedEffect

        if (hasNotificationPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionFinished = true
        } else if (!requestedNotificationPermission) {
            requestedNotificationPermission = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (!locationPermissionFinished || !notificationPermissionFinished) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Setting up permissions...")
        }
        return
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

        if (viewModel.routeIsActive) {
            RouteBottomSheet(
                etaMinutes = null,
                destinationName = viewModel.selectedDestinationName,
                upcomingStops = emptyList(),
                nearbyPlaces = emptyList(),
                onEndTrip = viewModel::endTrip,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (viewModel.shouldPromptForBackgroundAlerts) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissBackgroundAlertsPrompt() },
                title = { Text("Enable background alerts") },
                text = {
                    Text(
                        "EkiWatch needs background location to notify you when you are close to your destination, even if the app is not open."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val hasBackgroundLocation =
                                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED

                            if (hasBackgroundLocation) {
                                viewModel.registerSelectedDestinationGeofence()
                            } else {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    )
                                )
                            }
                        }
                    ) {
                        Text("Enable")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissBackgroundAlertsPrompt() }) {
                        Text("Not now")
                    }
                }
            )
        }
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

    LaunchedEffect(viewModel.searchResetSignal) {
        if (viewModel.searchResetSignal > 0) {
            active = false
            text = ""
        }
    }

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
                        val destinationName = place.getPrimaryText(null).toString()
                        text = destinationName

                        viewModel.resolveAndSelectDestination(place.placeId, destinationName)
                    }
                )

            }
        }
    }
}
