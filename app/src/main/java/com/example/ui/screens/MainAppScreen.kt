package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.viewmodel.FleetViewModel
import kotlinx.coroutines.launch

enum class AdminNavTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    LIVE_GPS("Live GPS", Icons.Default.MyLocation),
    POINTS("Points", Icons.Default.Place),
    ROUTE("Route", Icons.Default.AltRoute),
    REPORTS("Reports", Icons.Default.Assessment),
    AI_ASSISTANT("Swachh AI", Icons.Default.AutoAwesome),
    ALERTS("Alerts", Icons.Default.Notifications)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: FleetViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val villages by viewModel.villages.collectAsStateWithLifecycle()
    val selectedVillage by viewModel.selectedVillage.collectAsStateWithLifecycle()
    val points by viewModel.villagePoints.collectAsStateWithLifecycle()
    val allPoints by viewModel.collectionPoints.collectAsStateWithLifecycle()
    val vans by viewModel.vans.collectAsStateWithLifecycle()
    val selectedVan by viewModel.selectedVan.collectAsStateWithLifecycle()
    val staffList by viewModel.staffList.collectAsStateWithLifecycle()
    val routes by viewModel.routes.collectAsStateWithLifecycle()
    val collectionLogs by viewModel.collectionLogs.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val unreadAlertCount by viewModel.unreadAlertCount.collectAsStateWithLifecycle()
    val completedCount by viewModel.todayCompletedCount.collectAsStateWithLifecycle()
    val missedCount by viewModel.todayMissedCount.collectAsStateWithLifecycle()
    val pendingCount by viewModel.todayPendingCount.collectAsStateWithLifecycle()
    val totalWasteKg by viewModel.totalWasteCollectedKg.collectAsStateWithLifecycle()
    val wetWasteKg by viewModel.wetWasteCollectedKg.collectAsStateWithLifecycle()
    val dryWasteKg by viewModel.dryWasteCollectedKg.collectAsStateWithLifecycle()
    val isSimulationActive by viewModel.isGpsSimulationActive.collectAsStateWithLifecycle()
    val simulationSpeed by viewModel.simulationSpeedMultiplier.collectAsStateWithLifecycle()
    val useDeviceRealGps by viewModel.useDeviceRealGps.collectAsStateWithLifecycle()

    val driverVan by viewModel.driverVan.collectAsStateWithLifecycle()
    val driverRoute by viewModel.driverRoute.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(AdminNavTab.DASHBOARD) }

    // Dialog state holders
    var pointToComplete by remember { mutableStateOf<CollectionPoint?>(null) }
    var pointToMiss by remember { mutableStateOf<CollectionPoint?>(null) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showAddPointDialog by remember { mutableStateOf(false) }
    var showAddVanDialog by remember { mutableStateOf(false) }
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var showAddVillageDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF047857)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "GramSwachh Fleet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (currentUser != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                }
                            }
                            Text(
                                text = if (currentRole == UserRole.ADMIN_SUPERVISOR) "Supervisor Console • Firestore Sync" else "Driver Field Console",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Google Sign-In button or User Profile Icon
                    if (currentUser == null) {
                        Button(
                            onClick = {
                                activity?.let { act ->
                                    viewModel.signInWithGoogle(
                                        act,
                                        onSuccess = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Signed in with Google! Firestore sync active.")
                                            }
                                        },
                                        onError = { err ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(err)
                                            }
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("btn_google_signin"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier.testTag("btn_user_profile")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.displayName?.take(1)?.uppercase() ?: "U",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Role Toggle Switcher
                    FilledTonalButton(
                        onClick = {
                            val newRole = if (currentRole == UserRole.ADMIN_SUPERVISOR) UserRole.DRIVER_FIELD else UserRole.ADMIN_SUPERVISOR
                            viewModel.switchRole(newRole)
                        },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("btn_switch_role"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (currentRole == UserRole.DRIVER_FIELD) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            if (currentRole == UserRole.ADMIN_SUPERVISOR) Icons.Default.DirectionsCar else Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (currentRole == UserRole.DRIVER_FIELD) Color(0xFFB45309) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (currentRole == UserRole.ADMIN_SUPERVISOR) "Driver" else "Admin",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentRole == UserRole.DRIVER_FIELD) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Notification Bell
                    IconButton(
                        onClick = { activeTab = AdminNavTab.ALERTS },
                        modifier = Modifier.testTag("btn_top_alerts")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadAlertCount > 0) {
                                    Badge(containerColor = Color(0xFFEF4444)) {
                                        Text("$unreadAlertCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (currentRole == UserRole.ADMIN_SUPERVISOR) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    AdminNavTab.values().forEach { tab ->
                        val isSelected = activeTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { activeTab = tab },
                            icon = {
                                if (tab == AdminNavTab.ALERTS && unreadAlertCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = Color(0xFFEF4444)) {
                                                Text("$unreadAlertCount")
                                            }
                                        }
                                    ) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            } else {
                // Driver Mode Bottom Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                            Text("GPS: Active", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }

                        // Driver quick Swachh AI Live Voice button
                        OutlinedButton(
                            onClick = {
                                viewModel.switchRole(UserRole.ADMIN_SUPERVISOR)
                                activeTab = AdminNavTab.AI_ASSISTANT
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Live Voice Co-Pilot", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(onClick = { viewModel.switchRole(UserRole.ADMIN_SUPERVISOR) }) {
                            Text("Admin View", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentRole == UserRole.ADMIN_SUPERVISOR) {
                when (activeTab) {
                    AdminNavTab.DASHBOARD -> AdminDashboardScreen(
                        villages = villages,
                        selectedVillage = selectedVillage,
                        points = points,
                        vans = vans,
                        staffList = staffList,
                        alerts = alerts,
                        completedCount = completedCount,
                        missedCount = missedCount,
                        pendingCount = pendingCount,
                        totalWasteKg = totalWasteKg,
                        onSelectVillage = { viewModel.selectVillage(it) },
                        onSelectVan = { viewModel.selectVan(it) },
                        onNavigateToGps = { activeTab = AdminNavTab.LIVE_GPS },
                        onNavigateToPoints = { activeTab = AdminNavTab.POINTS },
                        onNavigateToAlerts = { activeTab = AdminNavTab.ALERTS },
                        onAddPointClick = { showAddPointDialog = true },
                        onAddVanClick = { showAddVanDialog = true },
                        onAddStaffClick = { showAddStaffDialog = true },
                        onAddVillageClick = { showAddVillageDialog = true }
                    )

                    AdminNavTab.LIVE_GPS -> LiveGpsTrackingScreen(
                        vans = vans,
                        selectedVan = selectedVan,
                        points = points,
                        staffList = staffList,
                        villageName = selectedVillage?.name ?: "Shivaji Nagar",
                        isSimulationActive = isSimulationActive,
                        simulationSpeed = simulationSpeed,
                        useDeviceRealGps = useDeviceRealGps,
                        onSelectVan = { viewModel.selectVan(it) },
                        onToggleSimulation = { viewModel.toggleGpsSimulation() },
                        onSetSimulationSpeed = { viewModel.setSimulationSpeed(it) },
                        onToggleRealGps = { ctx, enabled -> viewModel.toggleDeviceRealGps(ctx, enabled) }
                    )

                    AdminNavTab.POINTS -> VillageCollectionPointsScreen(
                        village = selectedVillage,
                        points = points,
                        onAddPointClick = { showAddPointDialog = true }
                    )

                    AdminNavTab.ROUTE -> RoutePlanningScreen(
                        routes = routes,
                        selectedRoute = viewModel.activeRouteForSelectedVan.collectAsStateWithLifecycle().value,
                        points = allPoints,
                        vans = vans,
                        staffList = staffList
                    )

                    AdminNavTab.REPORTS -> MonitoringReportsScreen(
                        village = selectedVillage,
                        points = points,
                        vans = vans,
                        staffList = staffList,
                        logs = collectionLogs,
                        totalWasteKg = totalWasteKg,
                        wetWasteKg = wetWasteKg,
                        dryWasteKg = dryWasteKg
                    )

                    AdminNavTab.AI_ASSISTANT -> SwachhAiAssistantScreen(
                        aiService = viewModel.aiService,
                        villageName = selectedVillage?.name ?: "Shivaji Nagar"
                    )

                    AdminNavTab.ALERTS -> AlertsNotificationsScreen(
                        alerts = alerts,
                        onResolveAlert = { viewModel.resolveAlert(it) }
                    )
                }
            } else {
                // DRIVER FIELD MODE
                DriverFieldModeScreen(
                    driverVan = driverVan,
                    driverRoute = driverRoute,
                    points = allPoints,
                    onMarkCompletedClick = { pt -> pointToComplete = pt },
                    onMarkMissedClick = { pt -> pointToMiss = pt },
                    onSosClick = { showSosDialog = true }
                )
            }
        }
    }

    // Modal Dialogs
    pointToComplete?.let { pt ->
        MarkCompletedDialog(
            point = pt,
            onDismiss = { pointToComplete = null },
            onConfirm = { wetKg, dryKg, hazKg, remarks ->
                viewModel.markPointCompleted(pt.id, wetKg, dryKg, hazKg, remarks)
                pointToComplete = null
                scope.launch {
                    snackbarHostState.showSnackbar("Logged ${wetKg + dryKg + hazKg}kg waste to Firestore!")
                }
            }
        )
    }

    pointToMiss?.let { pt ->
        MarkMissedDialog(
            point = pt,
            onDismiss = { pointToMiss = null },
            onConfirm = { reason, remarks ->
                viewModel.markPointMissed(pt.id, reason, remarks)
                pointToMiss = null
                scope.launch {
                    snackbarHostState.showSnackbar("Logged missed point to Firestore & triggered supervisor alert.")
                }
            }
        )
    }

    if (showSosDialog) {
        driverVan?.let { van ->
            SosEmergencyDialog(
                van = van,
                onDismiss = { showSosDialog = false },
                onConfirm = { issue, notes ->
                    viewModel.triggerBreakdownSos(van.id, issue, notes)
                    showSosDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Emergency SOS alert broadcasted to Gram Panchayat!")
                    }
                }
            )
        }
    }

    if (showAddPointDialog) {
        AddCollectionPointDialog(
            villages = villages,
            selectedVillageId = selectedVillage?.id ?: "VIL-01",
            onDismiss = { showAddPointDialog = false },
            onConfirm = { vId, name, lm, type, lat, lng, cap, time, types ->
                viewModel.addCollectionPoint(vId, name, lm, type, lat, lng, cap, time, types)
                showAddPointDialog = false
            }
        )
    }

    if (showAddVanDialog) {
        AddVanDialog(
            drivers = staffList,
            villages = villages,
            onDismiss = { showAddVanDialog = false },
            onConfirm = { reg, name, model, driverId, vId, cap ->
                viewModel.addVan(reg, name, model, driverId, vId, cap)
                showAddVanDialog = false
            }
        )
    }

    if (showAddStaffDialog) {
        AddStaffDialog(
            vans = vans,
            onDismiss = { showAddStaffDialog = false },
            onConfirm = { name, role, phone, assignedVanId ->
                viewModel.addStaff(name, role, phone, assignedVanId)
                showAddStaffDialog = false
            }
        )
    }

    if (showAddVillageDialog) {
        AddVillageDialog(
            onDismiss = { showAddVillageDialog = false },
            onConfirm = { name, gp, taluka, dist, wards, pop, sarpanch, phone, lat, lng ->
                viewModel.addVillage(name, gp, taluka, dist, wards, pop, sarpanch, phone, lat, lng)
                showAddVillageDialog = false
            }
        )
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Panchayat Account & Firebase", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("User: ${currentUser?.displayName ?: "Staff Member"}", fontWeight = FontWeight.Bold)
                    Text("Email: ${currentUser?.email ?: "Not set"}", style = MaterialTheme.typography.bodySmall)
                    Text("Firestore: Connected", style = MaterialTheme.typography.bodySmall, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                    Text("Database ID: ${context.getString(com.example.R.string.firestore_database_id)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.signOut {
                            showProfileDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Signed out of Google session.")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) { Text("Close") }
            }
        )
    }
}
