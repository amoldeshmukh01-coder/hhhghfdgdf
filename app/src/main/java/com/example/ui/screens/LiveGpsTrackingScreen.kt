package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*

@Composable
fun LiveGpsTrackingScreen(
    vans: List<Van>,
    selectedVan: Van?,
    points: List<CollectionPoint>,
    staffList: List<Staff>,
    villageName: String,
    isSimulationActive: Boolean,
    simulationSpeed: Int,
    useDeviceRealGps: Boolean,
    onSelectVan: (String) -> Unit,
    onToggleSimulation: () -> Unit,
    onSetSimulationSpeed: (Int) -> Unit,
    onToggleRealGps: (Context, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPoint by remember { mutableStateOf<CollectionPoint?>(null) }
    val assignedDriver = staffList.find { it.id == selectedVan?.assignedDriverId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Van Fleet Selector Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(vans) { van ->
                val isSelected = van.id == selectedVan?.id
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectVan(van.id) },
                    label = {
                        Text(
                            text = "${van.name} (${van.speedKmH.toInt()} km/h)",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.DirectionsBus,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (van.status == VanStatus.ON_ROUTE) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // 2. Simulation & Real GPS Control Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simulation Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onToggleSimulation,
                        modifier = Modifier.testTag("btn_toggle_simulation"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSimulationActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Icon(
                            if (isSimulationActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isSimulationActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isSimulationActive) "Pause Sim" else "Simulate Route",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimulationActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Speed multiplier
                    if (isSimulationActive) {
                        listOf(1, 2, 5).forEach { spd ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (simulationSpeed == spd) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { onSetSimulationSpeed(spd) }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${spd}x",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (simulationSpeed == spd) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Device Real GPS Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Phone GPS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = useDeviceRealGps,
                        onCheckedChange = { onToggleRealGps(context, it) },
                        modifier = Modifier.testTag("switch_device_gps")
                    )
                }
            }
        }

        // 3. Interactive Vector Map Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LiveGpsMapCanvas(
                points = points,
                activeVan = selectedVan,
                selectedPoint = selectedPoint,
                onPointSelected = { selectedPoint = it },
                modifier = Modifier.fillMaxSize()
            )

            // Inspect Card on Tapped Point
            selectedPoint?.let { pt ->
                ElevatedCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${pt.code}: ${pt.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${pt.landmark} • Sched: ${pt.scheduledTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { selectedPoint = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(status = pt.status)
                            Text(
                                text = "Bin Cap: ${pt.binCapacityKg} kg",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 4. Van Telemetry Bottom HUD
        selectedVan?.let { van ->
            VanTelemetryHud(
                van = van,
                driver = assignedDriver,
                villageName = villageName,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp)
            )
        }
    }
}
