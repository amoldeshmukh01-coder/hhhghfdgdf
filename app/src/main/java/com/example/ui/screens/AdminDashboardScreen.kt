package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    villages: List<Village>,
    selectedVillage: Village?,
    points: List<CollectionPoint>,
    vans: List<Van>,
    staffList: List<Staff>,
    alerts: List<AlertNotification>,
    completedCount: Int,
    missedCount: Int,
    pendingCount: Int,
    totalWasteKg: Double,
    onSelectVillage: (String) -> Unit,
    onSelectVan: (String) -> Unit,
    onNavigateToGps: () -> Unit,
    onNavigateToPoints: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onAddPointClick: () -> Unit,
    onAddVanClick: () -> Unit,
    onAddStaffClick: () -> Unit,
    onAddVillageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unreadAlerts = alerts.filter { !it.isResolved }
    val totalPoints = points.size.coerceAtLeast(1)
    val completionPercent = ((completedCount.toFloat() / totalPoints.toFloat()) * 100).toInt()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // 1. Gram Panchayat Village Selector Pills
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected Gram Panchayat",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onAddVillageClick) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Village", fontSize = 12.sp)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(villages) { village ->
                        val isSelected = village.id == selectedVillage?.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectVillage(village.id) },
                            label = { Text(village.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationCity,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // 2. Critical Alert Banner if any
        if (unreadAlerts.isNotEmpty()) {
            item {
                val latestAlert = unreadAlerts.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAlerts() }
                        .testTag("admin_alert_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (latestAlert.severity == AlertSeverity.CRITICAL) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (latestAlert.severity == AlertSeverity.CRITICAL) Color(0xFFEF4444) else Color(0xFFF59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${latestAlert.title} (${latestAlert.timestamp})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (latestAlert.severity == AlertSeverity.CRITICAL) Color(0xFF991B1B) else Color(0xFF92400E)
                            )
                            Text(
                                text = latestAlert.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (latestAlert.severity == AlertSeverity.CRITICAL) Color(0xFF7F1D1D) else Color(0xFF78350F),
                                maxLines = 1
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "View Alerts",
                            tint = if (latestAlert.severity == AlertSeverity.CRITICAL) Color(0xFF991B1B) else Color(0xFF92400E)
                        )
                    }
                }
            }
        }

        // 3. Key Metrics KPI Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Today's Collection Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Progress Bar Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Route Completion Rate",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$completionPercent%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (completionPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$completedCount Cleaned",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$pendingCount In Progress",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$missedCount Missed",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Active Vans",
                        value = "${vans.count { it.status == VanStatus.ON_ROUTE }} / ${vans.size}",
                        subtitle = "On Rural Routes",
                        icon = Icons.Default.LocalShipping,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Waste Cleared",
                        value = "${"%.1f".format(totalWasteKg)} kg",
                        subtitle = "Segregated Today",
                        icon = Icons.Default.DeleteSweep,
                        iconTint = Color(0xFF0D9488),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Village Information Card
        selectedVillage?.let { village ->
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = village.gramPanchayat,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Taluka: ${village.blockTaluka} • Dist: ${village.district}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AssistChip(
                                onClick = onNavigateToPoints,
                                label = { Text("Points") },
                                leadingIcon = { Icon(Icons.Default.Place, null, modifier = Modifier.size(16.dp)) }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Sarpanch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(village.sarpanchName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Supervisor Contact", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(village.supervisorPhone, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("Population / Wards", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${village.population} pop • ${village.totalWards} wards", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // 5. Van Fleet Status Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Garbage Collection Fleet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToGps) {
                    Text("Live Map", fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        items(vans) { van ->
            val driver = staffList.find { it.id == van.assignedDriverId }
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSelectVan(van.id)
                        onNavigateToGps()
                    }
                    .testTag("van_card_${van.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = van.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            VanStatusBadge(status = van.status)
                        }
                        Text(
                            text = "${van.regNumber} • Driver: ${driver?.name ?: "Assigned"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Speed: ${van.speedKmH.toInt()} km/h",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Odo: ${van.odometerKm} km",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Track on map",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 6. Management Quick Actions
        item {
            Text(
                text = "Quick Management Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onAddPointClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_quick_add_point"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Point", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onAddVanClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_quick_add_van"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Van", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onAddStaffClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Staff", fontSize = 12.sp)
                }
            }
        }
    }
}
