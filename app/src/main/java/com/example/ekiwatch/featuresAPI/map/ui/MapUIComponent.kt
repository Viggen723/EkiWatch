package com.example.ekiwatch.featuresAPI.map.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
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
                .fillMaxSize()
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchComponent(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    // 1. Use rememberSaveable to keep active/text state alive
    var active by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun dismissSearch() {
        active = false
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    LaunchedEffect(viewModel.searchResetSignal) {
        if (viewModel.searchResetSignal > 0) {
            text = ""
            dismissSearch()
        }
    }

    Box(modifier = modifier) {
        if (active) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = { dismissSearch() }
                    )
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(BgDeep.copy(alpha = 0.92f))
                    .border(1.dp, LineColor.copy(alpha = 0.85f), RoundedCornerShape(22.dp))
                    .clickable { active = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = Teal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = text,
                    onValueChange = { newText ->
                        text = newText
                        active = true
                        viewModel.onSearchQueryChanged(newText)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) active = true
                        },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Ink,
                        fontSize = 16.sp
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { dismissSearch() }
                    ),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = "Where ya off too?",
                                color = InkFaint,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

            if (active && viewModel.searchResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .heightIn(max = 280.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(BgDeep.copy(alpha = 0.96f))
                        .border(1.dp, LineColor.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
                ) {
                    items(viewModel.searchResults) { place ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = place.getPrimaryText(null).toString(),
                                    color = Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = place.getSecondaryText(null).toString(),
                                    color = InkDim,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Surface.copy(alpha = 0.96f),
                                headlineColor = Ink,
                                supportingColor = InkDim
                            ),
                            modifier = Modifier.clickable {
                                val destinationName = place.getPrimaryText(null).toString()
                                text = destinationName
                                dismissSearch()

                                viewModel.resolveAndSelectDestination(place.placeId, destinationName)
                            }
                        )
                    }
                }
            }
        }
    }
}
