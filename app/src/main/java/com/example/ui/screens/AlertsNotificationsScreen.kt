package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.AlertNotification
import com.example.data.model.AlertSeverity

@Composable
fun AlertsNotificationsScreen(
    alerts: List<AlertNotification>,
    onResolveAlert: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<String>("ACTIVE") }

    val filteredAlerts = remember(alerts, selectedFilter) {
        when (selectedFilter) {
            "ACTIVE" -> alerts.filter { !it.isResolved }
            "CRITICAL" -> alerts.filter { it.severity == AlertSeverity.CRITICAL }
            "RESOLVED" -> alerts.filter { it.isResolved }
            else -> alerts
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Fleet Alerts & Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-time route violations, idle stoppages, and SOS alerts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Filter chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val activeCount = alerts.count { !it.isResolved }
                    FilterChip(
                        selected = selectedFilter == "ACTIVE",
                        onClick = { selectedFilter = "ACTIVE" },
                        label = { Text("Active Unresolved ($activeCount)") }
                    )
                }
                item {
                    val criticalCount = alerts.count { it.severity == AlertSeverity.CRITICAL }
                    FilterChip(
                        selected = selectedFilter == "CRITICAL",
                        onClick = { selectedFilter = "CRITICAL" },
                        label = { Text("Critical ($criticalCount)") }
                    )
                }
                item {
                    val allCount = alerts.size
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All ($allCount)") }
                    )
                }
                item {
                    val resolvedCount = alerts.count { it.isResolved }
                    FilterChip(
                        selected = selectedFilter == "RESOLVED",
                        onClick = { selectedFilter = "RESOLVED" },
                        label = { Text("Resolved ($resolvedCount)") }
                    )
                }
            }
        }

        if (filteredAlerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF10B981)
                        )
                        Text(
                            text = "All clear! No alerts for this filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredAlerts) { alert ->
                val (cardBg, iconTint, icon) = when (alert.severity) {
                    AlertSeverity.CRITICAL -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Icons.Default.Warning)
                    AlertSeverity.WARNING -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Icons.Default.ErrorOutline)
                    AlertSeverity.INFO -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), Icons.Default.Info)
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alert_item_${alert.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (alert.isResolved) MaterialTheme.colorScheme.surface else cardBg
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(iconTint.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                                }

                                Column {
                                    Text(
                                        text = alert.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${alert.vanName} • ${alert.timestamp}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (alert.isResolved) {
                                Surface(
                                    color = Color(0xFFD1FAE5),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Resolved",
                                        color = Color(0xFF065F46),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    color = iconTint.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = alert.severity.label,
                                        color = iconTint,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = alert.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!alert.isResolved) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { onResolveAlert(alert.id) },
                                    modifier = Modifier.testTag("btn_resolve_${alert.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Acknowledge & Resolve")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
