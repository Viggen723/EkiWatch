package com.example.ekiwatch.featuresAPI.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ekiwatch.data.local.settings.DistanceUnit
import com.example.ekiwatch.featuresAPI.settings.viewModel.SettingsViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsUI(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val geofenceRadiusMeters by viewModel.geofenceRadiusMeters.collectAsState()
    val distanceUnit by viewModel.distanceUnit.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .offset(y = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection(title = "Notifications") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Arrival notifications", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Alert me when I'm approaching a station",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
            }
        }

        SettingsSection(title = "Geofence radius") {
            Text(
                "Distance from a station before you're alerted: ${geofenceRadiusMeters.roundToInt()} m",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = geofenceRadiusMeters,
                onValueChange = { viewModel.setGeofenceRadiusMeters(it) },
                valueRange = 100f..1500f,
                steps = 27 // 50m increments across the range
            )
        }

        SettingsSection(title = "Distance unit") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DistanceUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = distanceUnit == unit,
                        onClick = { viewModel.setDistanceUnit(unit) },
                        label = { Text(if (unit == DistanceUnit.KILOMETERS) "Kilometers" else "Miles") }
                    )
                }
            }
        }

        HorizontalDivider()

        OutlinedButton(onClick = { viewModel.resetToDefaults() }) {
            Text("Reset to defaults")
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}