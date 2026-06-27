package com.example.ekiwatch.featuresAPI.recent.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ekiwatch.data.local.recentPlaces.RecentStationView
import com.example.ekiwatch.featuresAPI.recent.viewModel.RecentPlacesViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun RecentPlacesUI(
    modifier: Modifier = Modifier,
    viewModel: RecentPlacesViewModel = viewModel()
) {
    val recentStations by viewModel.recentStations.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (recentStations.isEmpty()) {
            EmptyVisitedMessage(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recentStations, key = { it.id }) { station ->
                    RecentStationCard(
                        station,
                        onRemove = { viewModel.removeRecent(station.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyVisitedMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
        Text(
            text = "No visited stations yet",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Stations you pass through will show up here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecentStationCard(
    station: RecentStationView,
    modifier: Modifier = Modifier,
    onRemove: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = station.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (!station.nameJa.isNullOrBlank()) {
                Text(
                    text = station.nameJa,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Visited " + formatVisitTime(station.lastVisitedAtEpochMillis),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Remove from Past Visited"
                )
            }
        }
    }
}

// TODO If we end up having more of these types of converters, lets make an object class for them
private fun formatVisitTime(epochMillis: Long): String {
    val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    return formatter.format(Date(epochMillis))
}