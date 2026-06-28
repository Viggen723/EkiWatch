package com.example.ekiwatch.featuresAPI.map.viewModel

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ekiwatch.MyApplication
import com.example.ekiwatch.data.local.EkiWatchDatabase
import com.example.ekiwatch.data.local.landmark.LandmarkCategory
import com.example.ekiwatch.data.local.landmark.LandmarkEntity
import com.example.ekiwatch.data.local.landmark.LandmarkRepository
import com.example.ekiwatch.data.local.settings.SettingsManager
import com.example.ekiwatch.data.location.MapRepository
import com.example.ekiwatch.data.local.station.StationEntity
import com.example.ekiwatch.data.remote.ekispert.EkispertClient
import com.example.ekiwatch.featuresAPI.map.routing.RoutingRepository
import com.example.ekiwatch.featuresAPI.map.routing.RoutesApiService
import com.example.ekiwatch.featuresAPI.notifications.GeofenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val database = EkiWatchDatabase.getDatabase(application)
    private val landmarkRepository = LandmarkRepository(database.landmarkDao())
    private val mapRepository = MapRepository(application)
    private val settingsManager = SettingsManager.getInstance(application)
    private val app = application as MyApplication
    private val placesClient = app.placesClient

    private val json = Json { ignoreUnknownKeys = true }

    private val routesApiService = Retrofit.Builder()
        .baseUrl("https://routes.googleapis.com/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(RoutesApiService::class.java)

    // Pass the service to the Repository
    val routingRepository = RoutingRepository(
        context = application,
        routesApiService = routesApiService
    )

    // Initialize the new Geofence Manager here
    private val geofenceManager = GeofenceManager(application)

    val cameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(LatLng(35.6812, 139.7671), 13f)
    )

    var walkingPolylinePoints by mutableStateOf<List<LatLng>>(emptyList())
        private set
    var shouldPromptForBackgroundAlerts by mutableStateOf(false)
        private set
    var routeIsActive by mutableStateOf(false)
        private set
    var selectedDestinationName by mutableStateOf<String?>(null)
        private set
    private var selectedDestination: LatLng? = null

    fun loadUserLocation() {
        viewModelScope.launch {
            val latLng = mapRepository.getCurrentLocation()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 13f)
            loadTrainRoute("高津", "大手町")
        }
    }

    fun loadTrainRoute(departureStationName: String, arrivalStationName: String) {
        viewModelScope.launch {
            try {
                /*
                Here, the Ekispert stuff will be managed loading it into the navigation screen
                val mockPoints = listOf(
                    LatLng(35.6025, 139.6272),
                    LatLng(35.6131, 139.6669),
                    LatLng(35.6450, 139.7022)
                )

                routingRepository.trainPolylinePoints.clear()
                routingRepository.trainPolylinePoints.addAll(mockPoints)

                val mockStations = mockPoints.mapIndexed { index, latLng ->
                    StationEntity(
                        id = "mock_station_$index",
                        ekispertCode = "999$index",
                        name = "Mock Station $index",
                        lat = latLng.latitude,
                        lon = latLng.longitude
                    )
                }

                database.stationDao().insertAll(mockStations)

                val testLandmarks = listOf(
                    LandmarkEntity(
                        id = "spot_1",
                        osmId = "node_123456",
                        name = "Komazawa Olympic Park",
                        nameJa = "駒沢オリンピック公園",
                        category = LandmarkCategory.ATTRACTION,
                        lat = 35.6140,
                        lon = 139.6670,
                        nearestStationId = "mock_station_1"
                    ),
                    LandmarkEntity(
                        id = "spot_2",
                        osmId = "node_789012",
                        name = "Local Museum",
                        nameJa = "郷土資料館",
                        category = LandmarkCategory.MUSEUM,
                        lat = 35.6135,
                        lon = 139.6660,
                        nearestStationId = "mock_station_1"
                    )
                )
                landmarkRepository.insertLandmarks(testLandmarks)

                geofenceManager.clearActiveGeofences()
                geofenceManager.registerStationGeofences(
                    stations = mockStations,
                    radiusMeters = settingsManager.geofenceRadiusMeters.value
                ) */

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var searchQuery by mutableStateOf("")
    val searchResults = mutableStateListOf<AutocompletePrediction>()
    var searchResetSignal by mutableStateOf(0)
        private set
    private var searchJob: kotlinx.coroutines.Job? = null
    private var routeRefreshJob: Job? = null
    private var lastRouteOrigin: LatLng? = null
    private var isRefreshingRoute = false
    private var routeGeneration = 0

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        searchJob?.cancel()

        if (query.length < 3) {
            searchResults.clear()
            return
        }

        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300)

            val token = AutocompleteSessionToken.newInstance()
            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    searchResults.clear()
                    searchResults.addAll(response.autocompletePredictions)
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("EkiWatch", "Places API error: ${e.message}")
                }
        }
    }

    fun selectDestination(origin: LatLng, destination: LatLng, generation: Int = routeGeneration) {
        viewModelScope.launch {
            // Check this order to make sure that it is matched for the train. Fixed redrawing issue
            try {
                val route = routingRepository.getWalkingRoute(origin, destination)
                if (generation != routeGeneration || !routeIsActive) return@launch
                Log.d("EkiWatch", "Route point count: ${route.size}")

                if (route.isNotEmpty()) {
                    walkingPolylinePoints = route.withOriginStart(origin)
                    lastRouteOrigin = origin
                    val boundsBuilder = LatLngBounds.builder()
                    boundsBuilder.include(origin)
                    route.forEach { boundsBuilder.include(it) }
                    boundsBuilder.include(destination)
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120)
                    )
                    Log.d("EkiWatch", "Camera animation triggered for route bounds")
                } else {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(destination, 15f)
                    )
                    Log.d("EkiWatch", "Camera animation triggered for destination")
                }
            } catch (e: Exception) {
                if (generation != routeGeneration || !routeIsActive) return@launch
                Log.d("EkiWatch", "Route fetch or camera animation failed: ${e.message}")
                cameraPositionState.position = CameraPosition.fromLatLngZoom(destination, 15f)
                Log.d("EkiWatch", "Camera move triggered for destination fallback")
            }
            if (generation != routeGeneration || !routeIsActive) return@launch
            startRouteRefreshTracking()
        }
    }

    private fun startRouteRefreshTracking() {
        if (routeRefreshJob?.isActive == true) return

        routeRefreshJob = viewModelScope.launch {
            Log.d("EkiWatch", "Route refresh tracking started")
            try {
                mapRepository.observeLocationUpdates().collect { currentOrigin ->
                    Log.d("EkiWatch", "location update received: $currentOrigin")
                    val destination = selectedDestination ?: return@collect
                    val previousOrigin = lastRouteOrigin

                    if (previousOrigin == null) {
                        lastRouteOrigin = currentOrigin
                        Log.d("EkiWatch", "distance moved from last route origin: 0.0")
                        return@collect
                    }

                    val distanceMoved = distanceMeters(previousOrigin, currentOrigin)
                    Log.d("EkiWatch", "distance moved from last route origin: $distanceMoved")
                    if (distanceMoved < 50f || isRefreshingRoute) return@collect

                    refreshRouteFromCurrentLocation(currentOrigin, destination)
                }
            } finally {
                Log.d("EkiWatch", "route refresh tracking cancelled")
            }
        }
    }

    private suspend fun refreshRouteFromCurrentLocation(currentOrigin: LatLng, destination: LatLng) {
        isRefreshingRoute = true
        try {
            Log.d("EkiWatch", "route refresh started")
            Log.d("EkiWatch", "current origin: $currentOrigin")
            Log.d("EkiWatch", "selected destination: $destination")

        val route = routingRepository.getWalkingRoute(currentOrigin, destination)
            Log.d("EkiWatch", "route point count: ${route.size}")
            if (route.isNotEmpty()) {
                walkingPolylinePoints = route.withOriginStart(currentOrigin)
                Log.d("EkiWatch", "Polyline updated from live origin")
                Log.d("EkiWatch", "Polyline state replaced from live origin")
                lastRouteOrigin = currentOrigin
            } else {
            Log.d("EkiWatch", "route refresh failure: empty route")
        }
        } catch (e: Exception) {
            Log.d("EkiWatch", "route refresh failure: ${e.message}", e)
        } finally {
            isRefreshingRoute = false
        }
    }

    private fun distanceMeters(from: LatLng, to: LatLng): Float {
        val result = FloatArray(1)
        Location.distanceBetween(
            from.latitude,
            from.longitude,
            to.latitude,
            to.longitude,
            result
        )
        return result[0]
    }

    private fun List<LatLng>.withOriginStart(origin: LatLng): List<LatLng> {
        val firstPoint = firstOrNull() ?: return this
        return if (distanceMeters(origin, firstPoint) < 5f) {
            this
        } else {
            listOf(origin) + this
        }
    }

    fun dismissBackgroundAlertsPrompt() {
        shouldPromptForBackgroundAlerts = false
    }

    fun registerSelectedDestinationGeofence() {
        val destination = selectedDestination ?: return
        geofenceManager.registerSelectedDestinationGeofence(
            destination = destination,
            radiusMeters = settingsManager.geofenceRadiusMeters.value
        )
        shouldPromptForBackgroundAlerts = false
    }

    fun endTrip() {
        routeGeneration++
        routeRefreshJob?.cancel()
        routeRefreshJob = null
        searchJob?.cancel()
        searchJob = null
        searchQuery = ""
        searchResults.clear()
        searchResetSignal++
        selectedDestination = null
        selectedDestinationName = null
        routeIsActive = false
        walkingPolylinePoints = emptyList()
        routingRepository.trainPolylinePoints.clear()
        lastRouteOrigin = null
        isRefreshingRoute = false
        shouldPromptForBackgroundAlerts = false
        geofenceManager.clearActiveGeofences()
        viewModelScope.launch {
            try {
                val currentLocation = mapRepository.getCurrentLocationOrNull()
                if (currentLocation != null) {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(currentLocation, 15f)
                    )
                }
            } catch (e: Exception) {
                Log.d("EkiWatch", "Could not recenter after ending trip: ${e.message}", e)
            }
        }
    }

    fun resolveAndSelectDestination(placeId: String, destinationName: String? = null) {
        Log.d("EkiWatch", "Selected placeId: $placeId")
        val placeFields = listOf(Place.Field.LOCATION)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val destination = response.place.location
                if (destination != null) {
                    Log.d("EkiWatch", "Destination LatLng: $destination")
                    selectedDestination = destination
                    selectedDestinationName = destinationName
                    routeIsActive = true
                    routeGeneration++
                    val generation = routeGeneration
                    shouldPromptForBackgroundAlerts = true
                    routeRefreshJob?.cancel()
                    routeRefreshJob = null
                    lastRouteOrigin = null
                    viewModelScope.launch {
                        val origin = mapRepository.getCurrentLocation()
                        Log.d("EkiWatch", "Origin LatLng: $origin")
                        selectDestination(origin, destination, generation)
                    }
                } else {
                    android.util.Log.e("EkiWatch", "Place location is null")
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("EkiWatch", "Place details fetch failed: ${e.message}")
            }
    }

    override fun onCleared() {
        routeRefreshJob?.cancel()
        super.onCleared()
    }
}
