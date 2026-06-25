package com.example.ekiwatch.featuresAPI.map.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ekiwatch.featuresAPI.map.viewModel.MapViewModel
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType


@Composable
fun MapViewComponent(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = MapViewModel()
) {
    // The Box acts as a container for the map and potentially overlays
    Box(
        modifier = modifier
            .fillMaxSize()
        ){

        GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = viewModel.cameraPositionState
            ) {
            // any markers on the map can be placed in here. See documentation
        }

        // ex. add title on top of the map inside Box
        Text(
            text = "EkiWatch",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
    }
}