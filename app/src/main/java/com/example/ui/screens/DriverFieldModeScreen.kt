package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*

@Composable
fun DriverFieldModeScreen(
    driverVan: Van?,
    driverRoute: CollectionRoute?,
    points: List<CollectionPoint>,
    onMarkCompletedClick: (CollectionPoint) -> Unit,
    onMarkMissedClick: (CollectionPoint) -> Unit,
    onSosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routePoints = driverRoute?.pointIds?.mapNotNull { pid ->
        points.find { it.id == pid }
    } ?: emptyList()

    // Find current active / target point
    val currentTargetPoint = routePoints.find { it.status == PointStatus.IN_PROGRESS }
        ?: routePoints.find { it.status == PointStatus.PENDING }
        ?: routePoints.lastOrNull()

    val completedPoints = routePoints.filter { it.status == PointStatus.COMPLETED }
    val remainingPoints = routePoints.filter { it.status == PointStatus.PENDING || it.status == PointStatus.IN_PROGRESS }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Driver Mode Header with SOS button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Text(
                            text = "Driver Field Console",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${driverVan?.name ?: "Van 01"} (${driverVan?.regNumber ?: "MH-14"})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // High-visibility SOS Button
                Button(
                    onClick = onSosClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_driver_sos")
                ) {
                    Icon(Icons.Default.Emergency, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("SOS / ALERT", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // 2. Active Stop Spotlight Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_current_stop_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "CURRENT ASSIGNED STOP",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        currentTargetPoint?.let {
                            StatusBadge(status = it.status)
                        }
                    }

                    currentTargetPoint?.let { pt ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = pt.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "${pt.code} • ${pt.landmark}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Timing and Bin details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Scheduled Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(pt.scheduledTime, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Bin Capacity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${pt.binCapacityKg} kg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Point Order", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("#${pt.sequenceOrder} of ${routePoints.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Accepted Waste categories
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Waste to Collect:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                pt.acceptedWasteTypes.forEach { wt ->
                                    WasteTypeChip(wasteType = wt)
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // Large Action Buttons for Driver
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onMarkMissedClick(pt) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("btn_driver_mark_missed"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Mark Missed", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onMarkCompletedClick(pt) },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(52.dp)
                                    .testTag("btn_driver_mark_completed"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("CLEANED & DONE", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } ?: run {
                        Text(
                            text = "🎉 All points completed for today's morning route!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 3. Shift Progress Counter
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Cleaned", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${completedPoints.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFFE2E8F0)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${remainingPoints.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFFE2E8F0)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Odometer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${driverVan?.odometerKm ?: 0.0} km", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Upcoming Collection Points in Route
        item {
            Text(
                text = "Upcoming Stops on Circuit",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(remainingPoints.filter { it.id != currentTargetPoint?.id }) { pt ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${pt.sequenceOrder}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = pt.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(text = "${pt.landmark} • Sched: ${pt.scheduledTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    StatusBadge(status = pt.status)
                }
            }
        }
    }
}
