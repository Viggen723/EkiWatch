package com.example.ekiwatch.featuresAPI.map.routing

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.ekiwatch.BuildConfig
import com.example.ekiwatch.data.remote.ekispert.EkispertClient
import com.example.ekiwatch.data.remote.ekispert.EkispertResultSet
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

// Gets the data from the APIs (Both the Ekispert and the Google Maps routing
class RoutingRepository(
    private val context: Context,
    private val routesApiService: RoutesApiService,
    val trainPolylinePoints: SnapshotStateList<LatLng> = mutableStateListOf()
) {
    suspend fun getWalkingRoute(origin: LatLng, destination: LatLng): List<LatLng> =
        withContext(Dispatchers.IO) {
            try {
                val request = RouteRequest(
                    origin = Waypoint(LocationData(LatLngData(origin.latitude, origin.longitude))),
                    destination = Waypoint(LocationData(LatLngData(destination.latitude, destination.longitude)))
                )

                val response = routesApiService.computeRoutes(
                    apiKey = BuildConfig.GOOGLE_MAPS_API_KEY,
                    request = request
                )

                val encodedPath = response.routes.firstOrNull()?.polyline?.encodedPolyline
                return@withContext if (encodedPath != null) {
                    PolyUtil.decode(encodedPath)
                } else {
                    emptyList()
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("EkiWatch", "API Error Body: $errorBody")
                emptyList()
            } catch (e: Exception) {
                android.util.Log.e("EkiWatch", "General Exception: ${e.message}")
                emptyList()
            }
        }

    // TODO We will use Ekispert with this
    suspend fun getTrainRoute(
        departureStationName: String,
        arrivalStationName: String
    ): EkispertResultSet? = withContext(Dispatchers.IO) {
        try {
            val response = EkispertClient.api.getRoute(
                apiKey = "Ekispert key (use buildConfig.)", // Replace with your actual BuildConfig key
                fromStation = departureStationName,
                toStation = arrivalStationName
            )

            if (response.isSuccessful) {
                return@withContext response.body()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }
}
