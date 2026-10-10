package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusBadge(status: PointStatus) {
    val (bgColor, textColor, icon) = when (status) {
        PointStatus.COMPLETED -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), Icons.Default.CheckCircle)
        PointStatus.IN_PROGRESS -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), Icons.Default.DirectionsRun)
        PointStatus.PENDING -> Triple(Color(0xFFDBEAFE), Color(0xFF1E40AF), Icons.Default.Schedule)
        PointStatus.MISSED -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), Icons.Default.Cancel)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = status.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun VanStatusBadge(status: VanStatus) {
    val (bgColor, textColor) = when (status) {
        VanStatus.ON_ROUTE -> Pair(Color(0xFFD1FAE5), Color(0xFF065F46))
        VanStatus.IDLE -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
        VanStatus.COMPLETED -> Pair(Color(0xFFE0E7FF), Color(0xFF3730A3))
        VanStatus.BREAKDOWN_SOS -> Pair(Color(0xFFFEE2E2), Color(0xFF991B1B))
    }

    Text(
        text = status.label,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor
    )
}

@Composable
fun WasteTypeChip(wasteType: WasteType) {
    val (bg, fg) = when (wasteType) {
        WasteType.WET_ORGANIC -> Pair(Color(0xFFECFDF5), Color(0xFF047857))
        WasteType.DRY_RECYCLABLE -> Pair(Color(0xFFEFF6FF), Color(0xFF1D4ED8))
        WasteType.HAZARDOUS_SANITARY -> Pair(Color(0xFFFEF2F2), Color(0xFFB91C1C))
        WasteType.E_WASTE -> Pair(Color(0xFFFFFBEB), Color(0xFFB45309))
    }
    Text(
        text = wasteType.label,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = fg
    )
}

@Composable
fun VanTelemetryHud(
    van: Van,
    driver: Staff?,
    villageName: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        Text(
                            text = van.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        VanStatusBadge(status = van.status)
                    }
                    Text(
                        text = "${van.regNumber} • ${van.model}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Driver snippet
                driver?.let {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Driver: ${it.phone}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Telemetry Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryItem(
                    icon = Icons.Default.Speed,
                    label = "Speed",
                    value = "${van.speedKmH.toInt()} km/h"
                )
                TelemetryItem(
                    icon = Icons.Default.Route,
                    label = "Odometer",
                    value = "${van.odometerKm} km"
                )
                TelemetryItem(
                    icon = Icons.Default.BatteryChargingFull,
                    label = "Battery / Fuel",
                    value = "${van.batteryOrFuelPercent}%"
                )
                TelemetryItem(
                    icon = Icons.Default.Sensors,
                    label = "GPS Signal",
                    value = "Online"
                )
            }

            // Coordinates & ping
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GPS: ${"%.4f".format(van.currentLat)}, ${"%.4f".format(van.currentLng)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Ping: ${van.lastPingTimestamp}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TelemetryItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Dialog: Mark Completed with waste weights
@Composable
fun MarkCompletedDialog(
    point: CollectionPoint,
    onDismiss: () -> Unit,
    onConfirm: (wetKg: Double, dryKg: Double, hazardousKg: Double, remarks: String) -> Unit
) {
    var wetKgText by remember { mutableStateOf("25.0") }
    var dryKgText by remember { mutableStateOf("15.0") }
    var hazardousKgText by remember { mutableStateOf("0.0") }
    var remarksText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Collection Completed", fontWeight = FontWeight.Bold)
                Text(
                    text = point.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Record Segregated Waste Collected (kg):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = wetKgText,
                    onValueChange = { wetKgText = it },
                    label = { Text("Wet Organic Waste (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_wet_kg"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dryKgText,
                    onValueChange = { dryKgText = it },
                    label = { Text("Dry Recyclable Waste (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_dry_kg"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = hazardousKgText,
                    onValueChange = { hazardousKgText = it },
                    label = { Text("Hazardous / Sanitary (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_hazardous_kg"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    label = { Text("Driver Remarks / Bin Condition") },
                    placeholder = { Text("e.g. Bin completely cleaned and disinfected") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wet = wetKgText.toDoubleOrNull() ?: 0.0
                    val dry = dryKgText.toDoubleOrNull() ?: 0.0
                    val haz = hazardousKgText.toDoubleOrNull() ?: 0.0
                    onConfirm(wet, dry, haz, remarksText)
                },
                modifier = Modifier.testTag("btn_confirm_completed")
            ) {
                Text("Confirm Collection")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog: Mark Missed with reason picker
@Composable
fun MarkMissedDialog(
    point: CollectionPoint,
    onDismiss: () -> Unit,
    onConfirm: (reason: String, remarks: String) -> Unit
) {
    val reasons = listOf(
        "Road Blocked / Religious Festival / Mandap",
        "No Waste Available / Bin Empty",
        "Vehicle Breakdown / Technical Fault",
        "Resident / Community Unavailable",
        "Heavy Rains / Waterlogged Road",
        "Inaccessible Narrow Alley"
    )
    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Report Missed Collection Point", fontWeight = FontWeight.Bold)
                Text(
                    text = "${point.code}: ${point.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Reason for Skipping:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = if (selectedReason == reason) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(if (selectedReason == reason) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedReason, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("btn_confirm_missed")
            ) {
                Text("Log Missed Point")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog: Emergency SOS / Breakdown for Driver
@Composable
fun SosEmergencyDialog(
    van: Van,
    onDismiss: () -> Unit,
    onConfirm: (issue: String, notes: String) -> Unit
) {
    val emergencyTypes = listOf(
        "Mechanical / Engine Breakdown",
        "Tyre Puncture / Flat Wheel",
        "Road Fully Blocked / Landslide",
        "Medical Emergency / Staff Injury",
        "Accident with Obstacle"
    )
    var selectedIssue by remember { mutableStateOf(emergencyTypes.first()) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text("Emergency SOS / Breakdown", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Notify Gram Panchayat Supervisor immediately:",
                    style = MaterialTheme.typography.bodyMedium
                )
                emergencyTypes.forEach { issue ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedIssue == issue,
                            onClick = { selectedIssue = issue }
                        )
                        Text(text = issue, style = MaterialTheme.typography.bodySmall)
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Current Location Landmark & Situation") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedIssue, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("btn_send_sos_confirm")
            ) {
                Text("SEND EMERGENCY ALERT")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
