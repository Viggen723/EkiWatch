package com.example.ekiwatch.featuresAPI.map.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val RowHeight = 44.dp

data class RouteStopUi(
    val name: String,
    val code: String? = null
)

data class NearbyPlaceUi(
    val name: String,
    val distanceText: String,
    val icon: ImageVector? = null
)

@Composable
fun RouteBottomSheet(
    etaMinutes: Int?,
    destinationName: String?,
    upcomingStops: List<RouteStopUi>,
    nearbyPlaces: List<NearbyPlaceUi>,
    onEndTrip: () -> Unit,
    modifier: Modifier = Modifier,
    nextStopName: String? = upcomingStops.firstOrNull()?.name
) {
    var expanded by remember { mutableStateOf(false) }
    var dragAmount by remember { mutableFloatStateOf(0f) }
    val cornerRadius by animateFloatAsState(
        targetValue = if (expanded) 26f else 22f,
        animationSpec = tween(250),
        label = "sheet-corner"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(BgDeep.copy(alpha = 0.92f))
            .border(1.dp, LineColor.copy(alpha = 0.85f), RoundedCornerShape(cornerRadius.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        expanded = dragAmount < -18f || (expanded && dragAmount < 18f)
                        dragAmount = 0f
                    },
                    onDragCancel = { dragAmount = 0f },
                    onVerticalDrag = { _, dragDelta ->
                        dragAmount += dragDelta
                    }
                )
            }
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(42.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(InkFaint.copy(alpha = 0.65f))
        )

        Spacer(Modifier.height(12.dp))

        CollapsedRouteHeader(
            etaMinutes = etaMinutes,
            title = nextStopName ?: destinationName ?: "Route active",
            expanded = expanded,
            onExpand = { expanded = true }
        )

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(220)) + slideInVertically(
                initialOffsetY = { it / 8 },
                animationSpec = tween(260, easing = FastOutSlowInEasing)
            ),
            exit = fadeOut(tween(160)) + slideOutVertically(
                targetOffsetY = { it / 8 },
                animationSpec = tween(160)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                LiveTripSection(
                    etaMinutes = etaMinutes,
                    destinationName = destinationName,
                    nextStopName = nextStopName,
                    upcomingStops = upcomingStops
                )

                NearbyDiscoveriesSection(
                    places = nearbyPlaces,
                    nearName = nextStopName ?: destinationName
                )

                EndTripButton(onEndTrip = onEndTrip)
            }
        }
    }
}

@Composable
private fun EndTripButton(onEndTrip: () -> Unit) {
    OutlinedButton(
        onClick = onEndTrip,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Coral,
            containerColor = Coral.copy(alpha = 0.12f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Coral.copy(alpha = 0.75f)
        )
    ) {
        Text(
            text = "End trip",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CollapsedRouteHeader(
    etaMinutes: Int?,
    title: String,
    expanded: Boolean,
    onExpand: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Arriving in ${etaMinutes?.toString() ?: "--"} min",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = title,
                color = InkDim,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier.clickable(onClick = onExpand),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!expanded) {
                Text(
                    text = "See details",
                    color = Teal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = null,
                tint = Teal,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = if (expanded) 180f else 0f }
            )
        }
    }
}

@Composable
private fun PulsingDot() {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val scale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse-scale"
    )
    val alpha by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse-alpha"
    )

    Box(
        modifier = Modifier.size(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .clip(CircleShape)
                .background(Teal)
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Teal)
        )
    }
}

@Composable
private fun LiveTripSection(
    etaMinutes: Int?,
    destinationName: String?,
    nextStopName: String?,
    upcomingStops: List<RouteStopUi>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Surface.copy(alpha = 0.96f))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulsingDot()
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "LIVE TRIP",
                    color = Teal,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = etaMinutes?.toString() ?: "--",
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
                Text(
                    text = "min away",
                    color = InkFaint,
                    fontSize = 10.5.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = nextStopName ?: destinationName ?: "Route active",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (nextStopName != null) "next stop" else "destination",
                color = InkFaint,
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.height(18.dp))

        if (upcomingStops.isEmpty()) {
            // TODO: Connect Ekispert API upcoming station/stop data here later.
            EmptyRoutePlaceholder()
        } else {
            TrackView(stops = upcomingStops, activeIndex = 0)
        }
    }
}

@Composable
private fun EmptyRoutePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BgDeep.copy(alpha = 0.45f))
            .border(1.dp, LineColor, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Upcoming stops will appear here",
            color = InkFaint,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun TrackView(
    stops: List<RouteStopUi>,
    activeIndex: Int,
    modifier: Modifier = Modifier
) {
    val animatedIndex by animateFloatAsState(
        targetValue = activeIndex.toFloat(),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "track-index"
    )
    val totalHeight = RowHeight * stops.size

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        Box(
            modifier = Modifier
                .padding(start = 21.dp)
                .width(2.dp)
                .height(totalHeight - 12.dp)
                .background(LineColor)
        )
        Box(
            modifier = Modifier
                .padding(start = 21.dp, top = 7.dp)
                .width(2.dp)
                .height(RowHeight * animatedIndex)
                .background(Brush.verticalGradient(listOf(Gold, Coral)))
        )
        Box(
            modifier = Modifier
                .padding(start = 14.dp)
                .offset(y = RowHeight * animatedIndex + 4.dp)
                .size(16.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Gold)
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            stops.forEachIndexed { index, stop ->
                StationRow(
                    stop = stop,
                    state = when {
                        index == activeIndex -> StationState.ACTIVE
                        index < activeIndex -> StationState.PASSED
                        else -> StationState.UPCOMING
                    }
                )
            }
        }
    }
}

private enum class StationState {
    ACTIVE,
    PASSED,
    UPCOMING
}

@Composable
private fun StationRow(
    stop: RouteStopUi,
    state: StationState
) {
    val nodeColor by animateColorAsState(
        targetValue = when (state) {
            StationState.ACTIVE -> Gold
            StationState.PASSED -> TealDim
            StationState.UPCOMING -> BgDeep
        },
        animationSpec = tween(300),
        label = "node-color"
    )
    val borderColor by animateColorAsState(
        targetValue = when (state) {
            StationState.ACTIVE -> Gold
            StationState.PASSED -> Teal
            StationState.UPCOMING -> InkFaint
        },
        animationSpec = tween(300),
        label = "border-color"
    )
    val labelColor by animateColorAsState(
        targetValue = if (state == StationState.ACTIVE) Ink else InkDim,
        animationSpec = tween(300),
        label = "label-color"
    )
    val scale by animateFloatAsState(
        targetValue = if (state == StationState.ACTIVE) 1.15f else 1f,
        label = "node-scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(RowHeight)
            .padding(start = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(BgDeep)
                .border(2.dp, borderColor, CircleShape)
                .background(nodeColor, CircleShape)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = stop.name,
            color = labelColor,
            fontWeight = if (state == StationState.ACTIVE) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.5.sp,
            modifier = Modifier.weight(1f)
        )
        if (stop.code != null) {
            Text(
                text = stop.code,
                color = InkFaint,
                fontSize = 10.5.sp
            )
        }
    }
}

@Composable
private fun NearbyDiscoveriesSection(
    places: List<NearbyPlaceUi>,
    nearName: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Nearby discoveries",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            if (nearName != null) {
                Text(
                    text = "near $nearName",
                    color = InkFaint,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        if (places.isEmpty()) {
            // TODO: Connect Google Places API or another places source here later.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface.copy(alpha = 0.96f))
                    .border(1.dp, LineColor, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nearby discoveries will appear here",
                    color = InkFaint,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(places, key = { it.name }) { place ->
                    NearbyPlaceCard(place)
                }
            }
        }
    }
}

@Composable
private fun NearbyPlaceCard(place: NearbyPlaceUi) {
    var visible by remember(place.name) { mutableStateOf(false) }

    LaunchedEffect(place.name) {
        visible = false
        delay(60)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(400)
        )
    ) {
        Column(
            modifier = Modifier
                .width(148.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Surface.copy(alpha = 0.96f))
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Gold, Color(0xFFB5740D)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = place.icon ?: Icons.Outlined.Place,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = place.name,
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = InkFaint,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = place.distanceText,
                    color = InkFaint,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Preview
@Composable
private fun RouteBottomSheetPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF233448))
            .padding(top = 120.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        RouteBottomSheet(
            etaMinutes = 8,
            destinationName = "Shibuya Station",
            upcomingStops = listOf(
                RouteStopUi("Sangen-jaya", "DT03"),
                RouteStopUi("Ikejiri-ohashi", "DT02"),
                RouteStopUi("Shibuya", "DT01")
            ),
            nearbyPlaces = listOf(
                NearbyPlaceUi("Hachiko Square", "220 m"),
                NearbyPlaceUi("Miyashita Park", "480 m")
            ),
            onEndTrip = {}
        )
    }
}
