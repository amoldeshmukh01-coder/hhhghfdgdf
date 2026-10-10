package com.example.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.remote.GeminiAiService
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.FleetRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FleetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FleetRepository.getInstance()
    private val authRepository = FirebaseAuthRepository(application.applicationContext)
    val aiService = GeminiAiService()

    // Auth & Cloud Sync
    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser
    val isCloudSyncing: StateFlow<Boolean> = authRepository.isCloudSyncing

    val currentRole = repository.currentRole
    val selectedVillageId = repository.selectedVillageId
    val selectedVanId = repository.selectedVanId
    val villages = repository.villages
    val collectionPoints = repository.collectionPoints
    val vans = repository.vans
    val staffList = repository.staffList
    val routes = repository.routes
    val collectionLogs = repository.collectionLogs
    val alerts = repository.alerts
    val isGpsSimulationActive = repository.isGpsSimulationActive
    val simulationSpeedMultiplier = repository.simulationSpeedMultiplier
    val useDeviceRealGps = repository.useDeviceRealGps

    // Selected Village
    val selectedVillage: StateFlow<Village?> = combine(villages, selectedVillageId) { vils, id ->
        vils.find { it.id == id } ?: vils.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Collection points for current selected village
    val villagePoints: StateFlow<List<CollectionPoint>> = combine(collectionPoints, selectedVillageId) { pts, vId ->
        pts.filter { it.villageId == vId }.sortedBy { it.sequenceOrder }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Van
    val selectedVan: StateFlow<Van?> = combine(vans, selectedVanId) { vanList, id ->
        vanList.find { it.id == id } ?: vanList.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Route for selected Van
    val activeRouteForSelectedVan: StateFlow<CollectionRoute?> = combine(routes, selectedVanId) { routeList, vanId ->
        routeList.find { it.vanId == vanId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Driver field mode: Active Route for Driver's Van (default VAN-01 for demo)
    val driverVan: StateFlow<Van?> = vans.map { list ->
        list.find { it.id == "VAN-01" } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val driverRoute: StateFlow<CollectionRoute?> = combine(routes, driverVan) { rList, dVan ->
        rList.find { it.vanId == dVan?.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Unresolved Alert count
    val unreadAlertCount: StateFlow<Int> = alerts.map { list ->
        list.count { !it.isResolved }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Computed Daily Stats
    val todayCompletedCount: StateFlow<Int> = collectionPoints.map { list ->
        list.count { it.status == PointStatus.COMPLETED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayMissedCount: StateFlow<Int> = collectionPoints.map { list ->
        list.count { it.status == PointStatus.MISSED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayPendingCount: StateFlow<Int> = collectionPoints.map { list ->
        list.count { it.status == PointStatus.PENDING || it.status == PointStatus.IN_PROGRESS }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalWasteCollectedKg: StateFlow<Double> = collectionPoints.map { list ->
        list.sumOf { it.wetKgCollected + it.dryKgCollected + it.hazardousKgCollected }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val wetWasteCollectedKg: StateFlow<Double> = collectionPoints.map { list ->
        list.sumOf { it.wetKgCollected }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dryWasteCollectedKg: StateFlow<Double> = collectionPoints.map { list ->
        list.sumOf { it.dryKgCollected }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Active simulation coroutine job
    private var simulationJob: Job? = null

    init {
        // Observe simulation toggle
        viewModelScope.launch {
            isGpsSimulationActive.collect { active ->
                if (active) {
                    startSimulationLoop()
                } else {
                    stopSimulationLoop()
                }
            }
        }
    }

    fun signInWithGoogle(activity: Activity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        authRepository.signInWithGoogle(activity, viewModelScope, onSuccess, onError)
    }

    fun signOut(onComplete: () -> Unit) {
        authRepository.signOut(viewModelScope, onComplete)
    }

    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (isActive) {
                val delayTime = (2000L / simulationSpeedMultiplier.value).coerceAtLeast(400L)
                delay(delayTime)
                val currentRoute = activeRouteForSelectedVan.value ?: routes.value.firstOrNull()
                if (currentRoute != null) {
                    repository.stepSimulation(currentRoute.id)
                }
            }
        }
    }

    private fun stopSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = null
    }

    fun switchRole(role: UserRole) {
        repository.setUserRole(role)
    }

    fun selectVillage(id: String) {
        repository.setSelectedVillageId(id)
    }

    fun selectVan(id: String) {
        repository.setSelectedVanId(id)
    }

    fun toggleGpsSimulation() {
        repository.setGpsSimulationActive(!isGpsSimulationActive.value)
    }

    fun setSimulationSpeed(speed: Int) {
        repository.setSimulationSpeedMultiplier(speed)
    }

    fun toggleDeviceRealGps(context: Context, enable: Boolean) {
        repository.setUseDeviceRealGps(enable)
        if (enable) {
            startRealGpsListening(context)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRealGpsListening(context: Context) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
                loc?.let { handleRealGpsLocation(it) }
            }
        } catch (_: Exception) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                lm?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    5f,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            handleRealGpsLocation(location)
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    }
                )
            } catch (_: Exception) { }
        }
    }

    private fun handleRealGpsLocation(location: Location) {
        val van = driverVan.value ?: return
        repository.updateVanTelemetry(
            vanId = van.id,
            lat = location.latitude,
            lng = location.longitude,
            speedKmH = (location.speed * 3.6f),
            heading = location.bearing
        )
        // Also persist to Firestore
        authRepository.saveTelemetryToFirestore(van.id, location.latitude, location.longitude, location.speed * 3.6f, location.bearing)
    }

    fun markPointCompleted(
        pointId: String,
        wetKg: Double,
        dryKg: Double,
        hazardousKg: Double,
        remarks: String?
    ) {
        repository.markPointCompleted(pointId, wetKg, dryKg, hazardousKg, remarks)
        val pt = collectionPoints.value.find { it.id == pointId }
        val vName = villages.value.find { it.id == pt?.villageId }?.name ?: "Village"
        authRepository.saveCollectionToFirestore(
            pointId = pointId,
            pointName = pt?.name ?: "Point",
            villageName = vName,
            status = "COMPLETED",
            wetKg = wetKg,
            dryKg = dryKg,
            hazardousKg = hazardousKg,
            notes = remarks
        )
    }

    fun markPointMissed(
        pointId: String,
        reason: String,
        remarks: String?
    ) {
        repository.markPointMissed(pointId, reason, remarks)
        val pt = collectionPoints.value.find { it.id == pointId }
        val vName = villages.value.find { it.id == pt?.villageId }?.name ?: "Village"
        authRepository.saveCollectionToFirestore(
            pointId = pointId,
            pointName = pt?.name ?: "Point",
            villageName = vName,
            status = "MISSED",
            wetKg = 0.0,
            dryKg = 0.0,
            hazardousKg = 0.0,
            notes = "$reason. ${remarks ?: ""}"
        )
    }

    fun triggerBreakdownSos(vanId: String, issueType: String, notes: String?) {
        repository.triggerBreakdownSos(vanId, issueType, notes)
    }

    fun resolveAlert(alertId: String) {
        repository.resolveAlert(alertId)
    }

    fun addCollectionPoint(
        villageId: String,
        name: String,
        landmark: String,
        type: PointType,
        latitude: Double,
        longitude: Double,
        capacityKg: Int,
        scheduledTime: String,
        acceptedWasteTypes: Set<WasteType>
    ) {
        repository.addCollectionPoint(
            villageId, name, landmark, type, latitude, longitude, capacityKg, scheduledTime, acceptedWasteTypes
        )
    }

    fun addVan(
        regNumber: String,
        name: String,
        model: String,
        assignedDriverId: String,
        assignedVillageId: String,
        capacityKg: Int
    ) {
        repository.addVan(regNumber, name, model, assignedDriverId, assignedVillageId, capacityKg)
    }

    fun addStaff(
        name: String,
        role: StaffRole,
        phone: String,
        assignedVanId: String?
    ) {
        repository.addStaff(name, role, phone, assignedVanId)
    }

    fun addVillage(
        name: String,
        gramPanchayat: String,
        blockTaluka: String,
        district: String,
        totalWards: Int,
        population: Int,
        sarpanchName: String,
        supervisorPhone: String,
        centerLat: Double,
        centerLng: Double
    ) {
        repository.addVillage(
            name, gramPanchayat, blockTaluka, district, totalWards, population, sarpanchName, supervisorPhone, centerLat, centerLng
        )
    }
}
