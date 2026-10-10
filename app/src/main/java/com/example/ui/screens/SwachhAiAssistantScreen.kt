package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.CompostScienceResult
import com.example.data.remote.GeminiAiService
import kotlinx.coroutines.launch

enum class AiFeatureTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MAPS_GROUNDING("Maps Grounding", Icons.Default.Place),
    SEARCH_GROUNDING("Search Grounding", Icons.Default.Search),
    LIVE_VOICE("Live Voice API", Icons.Default.Mic),
    SCIENCE_SKILLS("Science Lab", Icons.Default.Science)
}

@Composable
fun SwachhAiAssistantScreen(
    aiService: GeminiAiService,
    villageName: String,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(AiFeatureTab.MAPS_GROUNDING) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tab selector
        ScrollableTabRow(
            selectedTabIndex = activeTab.ordinal,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            AiFeatureTab.values().forEach { tab ->
                Tab(
                    selected = activeTab == tab,
                    onClick = { activeTab = tab },
                    text = { Text(tab.label, fontSize = 12.sp, fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                )
            }
        }

        when (activeTab) {
            AiFeatureTab.MAPS_GROUNDING -> MapsGroundingTab(aiService, villageName)
            AiFeatureTab.SEARCH_GROUNDING -> SearchGroundingTab(aiService)
            AiFeatureTab.LIVE_VOICE -> LiveVoiceTab(aiService)
            AiFeatureTab.SCIENCE_SKILLS -> ScienceSkillsTab(aiService)
        }
    }
}

@Composable
private fun MapsGroundingTab(aiService: GeminiAiService, villageName: String) {
    val scope = rememberCoroutineScope()
    var queryText by remember { mutableStateOf("Nearest solid waste processing yard, biogas plant, or compost pit near $villageName") }
    var responseText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Google Maps Grounding (gemini-3.5-flash)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Text(
                        text = "Query real-world locations, compost facilities, scrap recyclers, and rural dumpsites grounded directly with Google Maps data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        label = { Text("Ask Location or Route Point Query") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_maps_grounding_query"),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                responseText = aiService.queryWithMapsGrounding(queryText)
                                isLoading = false
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.testTag("btn_run_maps_grounding")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Querying Google Maps...")
                        } else {
                            Icon(Icons.Default.Explore, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Search Grounded Maps")
                        }
                    }
                }
            }
        }

        if (responseText.isNotBlank()) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Google Maps Grounded Output", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Text(text = responseText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchGroundingTab(aiService: GeminiAiService) {
    val scope = rememberCoroutineScope()
    var queryText by remember { mutableStateOf("Swachh Bharat Mission Grameen Phase 2 guidelines for rural garbage collection van subsidy and solid waste management") }
    var responseText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0284C7))
                        Text("Google Search Grounding (gemini-3.5-flash)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Text(
                        text = "Access live, up-to-date government circulars, sanitation guidelines, and rural waste policies backed by Google Search.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        label = { Text("Sanitation Policy or Guidelines Query") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_grounding_query"),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                responseText = aiService.queryWithSearchGrounding(queryText)
                                isLoading = false
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.testTag("btn_run_search_grounding")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Searching Live Web...")
                        } else {
                            Icon(Icons.Default.TravelExplore, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Search Grounded Data")
                        }
                    }
                }
            }
        }

        if (responseText.isNotBlank()) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFEFF6FF))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF1D4ED8))
                            Text("Google Search Grounded Result", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = Color(0xFF1E40AF))
                        }
                        Text(text = responseText, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1E293B))
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveVoiceTab(aiService: GeminiAiService) {
    val scope = rememberCoroutineScope()
    var isListening by remember { mutableStateOf(false) }
    var driverSpokenText by remember { mutableStateOf("Point 2 completed, road near temple is crowded with market vendors. What is the alternate route to School point?") }
    var assistantVoiceReply by remember { mutableStateOf("") }
    var isConnecting by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White)
                            }
                            Column {
                                Text("Live Voice Co-Pilot", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("Powered by gemini-3.8-live (Live API)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Surface(
                            color = if (isListening) Color(0xFFFEE2E2) else Color(0xFFD1FAE5),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (isListening) "Mic Active" else "Ready",
                                color = if (isListening) Color(0xFF991B1B) else Color(0xFF065F46),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "Real-time hands-free audio assistant for van drivers to navigate rural alleys, report blockages, and confirm collection progress by voice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = driverSpokenText,
                        onValueChange = { driverSpokenText = it },
                        label = { Text("Driver Voice Query Transcript") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isConnecting = true
                                scope.launch {
                                    assistantVoiceReply = aiService.queryLiveVoiceAssistant(driverSpokenText)
                                    isConnecting = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_start_live_voice"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (isConnecting) "Streaming..." else "Transmit to gemini-3.8-live")
                        }
                    }
                }
            }
        }

        if (assistantVoiceReply.isNotBlank()) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFF3E8FF))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF7C3AED))
                            Text("Live Spoken Response", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = Color(0xFF6D28D9))
                        }
                        Text(text = assistantVoiceReply, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScienceSkillsTab(aiService: GeminiAiService) {
    var wetKgText by remember { mutableStateOf("150") }
    var dryLeavesText by remember { mutableStateOf("50") }
    var result by remember { mutableStateOf<CompostScienceResult?>(null) }

    LaunchedEffect(wetKgText, dryLeavesText) {
        val wet = wetKgText.toDoubleOrNull() ?: 0.0
        val dry = dryLeavesText.toDoubleOrNull() ?: 0.0
        result = aiService.calculateCompostScience(wet, dry)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Biotech, contentDescription = null, tint = Color(0xFF059669))
                        Text("Waste Science & Composting Lab", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "Scientific formulation for rural solid waste composting, C:N stoichiometric equilibrium, and renewable biogas yield calculations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = wetKgText,
                            onValueChange = { wetKgText = it },
                            label = { Text("Wet Organic (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dryLeavesText,
                            onValueChange = { dryLeavesText = it },
                            label = { Text("Dry Leaves/Straw (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        result?.let { res ->
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (res.isBalanced) Color(0xFFECFDF5) else Color(0xFFFFFBEB)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bio-Chemical Balance:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Surface(
                                color = if (res.isBalanced) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (res.isBalanced) "Optimal C:N Ratio" else "Needs Adjustment",
                                    color = if (res.isBalanced) Color(0xFF065F46) else Color(0xFF92400E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Carbon : Nitrogen", style = MaterialTheme.typography.labelSmall)
                                Text("${res.cnRatio} : 1", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Moisture Level", style = MaterialTheme.typography.labelSmall)
                                Text("${res.moisturePercent}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Biogas Potential", style = MaterialTheme.typography.labelSmall)
                                Text("${res.biogasYieldM3} m³", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Text(
                            text = res.recommendation,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "💡 Bio-Energy Output: ${res.biogasYieldM3} m³ of biogas can displace approx ${res.lpgEquivalentKg} kg of LPG cylinder gas for Gram Panchayat mid-day meal kitchens.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
