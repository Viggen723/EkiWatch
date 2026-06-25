package com.example.ekiwatch.featuresAPI.map.viewModel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState

class MapViewModel : ViewModel() {
    // Manage map camera state here
    val cameraPositionState = CameraPositionState(
        // TODO Making it start on Tuj for now. Make it the users location
        position = CameraPosition.fromLatLngZoom(LatLng(35.642481, 139.673932), 18f)
    )
}