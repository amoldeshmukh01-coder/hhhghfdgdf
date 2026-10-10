package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge

@Composable
fun MonitoringReportsScreen(
    village: Village?,
    points: List<CollectionPoint>,
    vans: List<Van>,
    staffList: List<Staff>,
    logs: List<CollectionLog>,
    totalWasteKg: Double,
    wetWasteKg: Double,
    dryWasteKg: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val completedPoints = points.count { it.status == PointStatus.COMPLETED }
    val missedPoints = points.count { it.status == PointStatus.MISSED }
    val totalPoints = points.size.coerceAtLeast(1)
    val efficiencyRate = ((completedPoints.toFloat() / totalPoints.toFloat()) * 100).toInt()
    val totalFleetDistance = vans.sumOf { it.odometerKm }.let { (it * 10).toInt() / 10.0 }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Header with Export / Share button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Monitoring & Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${village?.name ?: "Gram Panchayat"} • Official Log",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        shareReport(context, village?.name ?: "Gram Panchayat", completedPoints, missedPoints, totalWasteKg, wetWasteKg, dryWasteKg, totalFleetDistance)
                    },
                    modifier = Modifier.testTag("btn_export_report"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share Report", fontSize = 12.sp)
                }
            }
        }

        // 2. Waste Segregation Breakdown Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Waste Segregation Analysis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Garbage Collected:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${"%.1f".format(totalWasteKg)} kg",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Wet vs Dry Progress split
                    val wetRatio = if (totalWasteKg > 0) (wetWasteKg / totalWasteKg).toFloat() else 0.5f
                    LinearProgressIndicator(
                        progress = { wetRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = Color(0xFF10B981), // Wet green
                        trackColor = Color(0xFF2563EB) // Dry blue
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFF10B981), CircleShape))
                            Text("Wet Organic: ${"%.1f".format(wetWasteKg)} kg", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFF2563EB), CircleShape))
                            Text("Dry Recyclable: ${"%.1f".format(dryWasteKg)} kg", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 3. Operational Performance KPI Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Route Efficiency",
                    value = "$efficiencyRate%",
                    subtitle = "$completedPoints of $totalPoints Points",
                    icon = Icons.Default.Analytics,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Fleet Distance",
                    value = "$totalFleetDistance km",
                    subtitle = "${vans.size} Vans Monitored",
                    icon = Icons.Default.AltRoute,
                    iconTint = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Staff & Driver Attendance Overview
        item {
            Text(
                text = "Staff Attendance & Duty Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(staffList) { staff ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = staff.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(text = "${staff.role.label} • ${staff.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Surface(
                        color = Color(0xFFD1FAE5),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = staff.attendanceStatus.label,
                            color = Color(0xFF065F46),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // 5. Audit Trail: Daily Collection Logs
        item {
            Text(
                text = "Live Collection Audit Logs",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(logs) { log ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = log.pointName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        StatusBadge(status = log.status)
                    }

                    Text(
                        text = "Time: ${log.timestamp} • Van: ${log.vanReg} • Driver: ${log.driverName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (log.status == PointStatus.COMPLETED) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Waste: ${log.totalWeightKg}kg (Wet: ${log.wetKg}kg, Dry: ${log.dryKg}kg)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    log.notes?.let { note ->
                        Text(
                            text = "Note: $note",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun shareReport(
    context: Context,
    villageName: String,
    completed: Int,
    missed: Int,
    totalKg: Double,
    wetKg: Double,
    dryKg: Double,
    distanceKm: Double
) {
    val report = """
        🌿 GramSwachh Rural Waste Collection Daily Report
        Village: $villageName
        Date: ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())}
        
        📊 Collection Summary:
        - Cleaned Points: $completed
        - Missed Points: $missed
        - Total Waste Cleared: ${"%.1f".format(totalKg)} kg
        - Wet Organic: ${"%.1f".format(wetKg)} kg
        - Dry Recyclable: ${"%.1f".format(dryKg)} kg
        - Total Fleet Travel: $distanceKm km
        
        Generated via GramSwachh Fleet Management
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, report)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Swachh Report"))
}
