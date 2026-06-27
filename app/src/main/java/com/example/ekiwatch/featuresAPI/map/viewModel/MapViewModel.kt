package com.example.ekiwatch.featuresAPI.map.viewModel

import android.app.Application
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.CameraPositionState
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

    val walkingPolylinePoints = mutableStateListOf<LatLng>()

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
    private var searchJob: kotlinx.coroutines.Job? = null

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

    fun selectDestination(origin: LatLng, destination: LatLng) {
        viewModelScope.launch {
            // Check this order to make sure that it is matched for the train. Fixed redrawing issue
            walkingPolylinePoints.clear()
            val route = routingRepository.getWalkingRoute(origin, destination)
            walkingPolylinePoints.addAll(route)
        }
    }

    fun resolveAndSelectDestination(placeId: String) {
        val placeFields = listOf(Place.Field.LOCATION)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val destination = response.place.location
                if (destination != null) {
                    viewModelScope.launch {
                        val origin = mapRepository.getCurrentLocation()
                        selectDestination(origin, destination)
                    }
                } else {
                    android.util.Log.e("EkiWatch", "Place location is null")
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("EkiWatch", "Place details fetch failed: ${e.message}")
            }
    }
}