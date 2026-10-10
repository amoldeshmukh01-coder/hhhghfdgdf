package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FleetRepository private constructor() {

    companion object {
        @Volatile
        private var instance: FleetRepository? = null

        fun getInstance(): FleetRepository {
            return instance ?: synchronized(this) {
                instance ?: FleetRepository().also { instance = it }
            }
        }
    }

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Current logged in / active user role
    private val _currentRole = MutableStateFlow(UserRole.ADMIN_SUPERVISOR)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Selected Village for filtering
    private val _selectedVillageId = MutableStateFlow("VIL-01")
    val selectedVillageId: StateFlow<String> = _selectedVillageId.asStateFlow()

    // Selected Van for Live Tracking
    private val _selectedVanId = MutableStateFlow("VAN-01")
    val selectedVanId: StateFlow<String> = _selectedVanId.asStateFlow()

    // GPS Simulation Mode
    private val _isGpsSimulationActive = MutableStateFlow(false)
    val isGpsSimulationActive: StateFlow<Boolean> = _isGpsSimulationActive.asStateFlow()

    private val _simulationSpeedMultiplier = MutableStateFlow(1)
    val simulationSpeedMultiplier: StateFlow<Int> = _simulationSpeedMultiplier.asStateFlow()

    // Real device GPS enabled for driver
    private val _useDeviceRealGps = MutableStateFlow(false)
    val useDeviceRealGps: StateFlow<Boolean> = _useDeviceRealGps.asStateFlow()

    // Data StateFlows
    private val _villages = MutableStateFlow<List<Village>>(emptyList())
    val villages: StateFlow<List<Village>> = _villages.asStateFlow()

    private val _collectionPoints = MutableStateFlow<List<CollectionPoint>>(emptyList())
    val collectionPoints: StateFlow<List<CollectionPoint>> = _collectionPoints.asStateFlow()

    private val _vans = MutableStateFlow<List<Van>>(emptyList())
    val vans: StateFlow<List<Van>> = _vans.asStateFlow()

    private val _staffList = MutableStateFlow<List<Staff>>(emptyList())
    val staffList: StateFlow<List<Staff>> = _staffList.asStateFlow()

    private val _routes = MutableStateFlow<List<CollectionRoute>>(emptyList())
    val routes: StateFlow<List<CollectionRoute>> = _routes.asStateFlow()

    private val _collectionLogs = MutableStateFlow<List<CollectionLog>>(emptyList())
    val collectionLogs: StateFlow<List<CollectionLog>> = _collectionLogs.asStateFlow()

    private val _alerts = MutableStateFlow<List<AlertNotification>>(emptyList())
    val alerts: StateFlow<List<AlertNotification>> = _alerts.asStateFlow()

    init {
        seedInitialData()
    }

    fun setUserRole(role: UserRole) {
        _currentRole.value = role
    }

    fun setSelectedVillageId(id: String) {
        _selectedVillageId.value = id
    }

    fun setSelectedVanId(id: String) {
        _selectedVanId.value = id
    }

    fun setGpsSimulationActive(active: Boolean) {
        _isGpsSimulationActive.value = active
    }

    fun setSimulationSpeedMultiplier(multiplier: Int) {
        _simulationSpeedMultiplier.value = multiplier
    }

    fun setUseDeviceRealGps(enabled: Boolean) {
        _useDeviceRealGps.value = enabled
        if (enabled) {
            _isGpsSimulationActive.value = false
        }
    }

    private fun seedInitialData() {
        val initialVillages = listOf(
            Village(
                id = "VIL-01",
                name = "Shivaji Nagar",
                gramPanchayat = "Shivaji Nagar Gram Panchayat",
                blockTaluka = "Haveli",
                district = "Pune",
                totalWards = 4,
                population = 3850,
                sarpanchName = "Sunanda R. Patil",
                supervisorPhone = "+91 98230 45120",
                centerLat = 18.5204,
                centerLng = 73.8567
            ),
            Village(
                id = "VIL-02",
                name = "Rampur Khurd",
                gramPanchayat = "Rampur Khurd Gram Panchayat",
                blockTaluka = "Haveli",
                district = "Pune",
                totalWards = 3,
                population = 2410,
                sarpanchName = "Kishor B. Jagtap",
                supervisorPhone = "+91 97645 11289",
                centerLat = 18.5350,
                centerLng = 73.8720
            ),
            Village(
                id = "VIL-03",
                name = "Kalyanpur",
                gramPanchayat = "Kalyanpur Gram Panchayat",
                blockTaluka = "Mulshi",
                district = "Pune",
                totalWards = 3,
                population = 1920,
                sarpanchName = "Dattatraya More",
                supervisorPhone = "+91 94220 88314",
                centerLat = 18.5080,
                centerLng = 73.8340
            )
        )

        val initialStaff = listOf(
            Staff(
                id = "STF-01",
                name = "Ramesh Patil",
                role = StaffRole.DRIVER,
                phone = "+91 98901 23456",
                assignedVanId = "VAN-01",
                attendanceStatus = AttendanceStatus.ON_DUTY,
                shiftsCompleted = 32
            ),
            Staff(
                id = "STF-02",
                name = "Suresh Pawar",
                role = StaffRole.DRIVER,
                phone = "+91 98223 98765",
                assignedVanId = "VAN-02",
                attendanceStatus = AttendanceStatus.ON_DUTY,
                shiftsCompleted = 28
            ),
            Staff(
                id = "STF-03",
                name = "Anil Shinde",
                role = StaffRole.DRIVER,
                phone = "+91 97631 54321",
                assignedVanId = "VAN-03",
                attendanceStatus = AttendanceStatus.PRESENT,
                shiftsCompleted = 21
            ),
            Staff(
                id = "STF-04",
                name = "Tukaram Jadhav",
                role = StaffRole.SANITATION_WORKER,
                phone = "+91 94210 67890",
                assignedVanId = "VAN-01",
                attendanceStatus = AttendanceStatus.ON_DUTY,
                shiftsCompleted = 30
            ),
            Staff(
                id = "STF-05",
                name = "Sunita Kamble",
                role = StaffRole.SANITATION_WORKER,
                phone = "+91 98500 33441",
                assignedVanId = "VAN-02",
                attendanceStatus = AttendanceStatus.ON_DUTY,
                shiftsCompleted = 29
            ),
            Staff(
                id = "STF-06",
                name = "Vijay Deshmukh",
                role = StaffRole.ROUTE_SUPERVISOR,
                phone = "+91 98230 45120",
                assignedVanId = null,
                attendanceStatus = AttendanceStatus.ON_DUTY,
                shiftsCompleted = 35
            )
        )

        val initialPoints = listOf(
            CollectionPoint(
                id = "CP-01",
                villageId = "VIL-01",
                code = "SHV-01",
                name = "North Gram Mandir Chowk",
                landmark = "Near Hanuman Mandir & Well",
                type = PointType.PUBLIC_BIN,
                latitude = 18.5204,
                longitude = 73.8567,
                sequenceOrder = 1,
                scheduledTime = "07:30 AM",
                acceptedWasteTypes = setOf(WasteType.WET_ORGANIC, WasteType.DRY_RECYCLABLE),
                binCapacityKg = 80,
                status = PointStatus.COMPLETED,
                completedAt = "07:38 AM",
                wetKgCollected = 34.5,
                dryKgCollected = 21.0,
                hazardousKgCollected = 0.5,
                driverRemarks = "Bin emptied and area swept"
            ),
            CollectionPoint(
                id = "CP-02",
                villageId = "VIL-01",
                code = "SHV-02",
                name = "Zilla Parishad Primary School",
                landmark = "School Gate #2 & Anganwadi",
                type = PointType.PRIMARY_SCHOOL,
                latitude = 18.5228,
                longitude = 73.8588,
                sequenceOrder = 2,
                scheduledTime = "08:00 AM",
                acceptedWasteTypes = setOf(WasteType.WET_ORGANIC, WasteType.DRY_RECYCLABLE),
                binCapacityKg = 60,
                status = PointStatus.COMPLETED,
                completedAt = "08:09 AM",
                wetKgCollected = 18.0,
                dryKgCollected = 26.5,
                hazardousKgCollected = 0.0,
                driverRemarks = "Collected midday meal organic waste"
            ),
            CollectionPoint(
                id = "CP-03",
                villageId = "VIL-01",
                code = "SHV-03",
                name = "Kisan Basti - Line 1 Residential",
                landmark = "House No. 1 to 24 Cluster",
                type = PointType.HOUSE_CLUSTER,
                latitude = 18.5245,
                longitude = 73.8612,
                sequenceOrder = 3,
                scheduledTime = "08:30 AM",
                acceptedWasteTypes = setOf(WasteType.WET_ORGANIC, WasteType.DRY_RECYCLABLE, WasteType.HAZARDOUS_SANITARY),
                binCapacityKg = 120,
                status = PointStatus.IN_PROGRESS,
                completedAt = null
            ),
            CollectionPoint(
                id = "CP-04",
                villageId = "VIL-01",
                code = "SHV-04",
                name = "Primary Health Sub-Centre",
                landmark = "Opposite Water Tank",
                type = PointType.HEALTH_SUB_CENTRE,
                latitude = 18.5262,
                longitude = 73.8590,
                sequenceOrder = 4,
                scheduledTime = "09:00 AM",
                acceptedWasteTypes = setOf(WasteType.HAZARDOUS_SANITARY, WasteType.DRY_RECYCLABLE),
                binCapacityKg = 40,
                status = PointStatus.PENDING
            ),
            CollectionPoint(
                id = "CP-05",
                villageId = "VIL-01",
                code = "SHV-05",
                name = "Weekly Haat Market Square",
                landmark = "Vegetable Shed Platform",
                type = PointType.MARKET_AREA,
                latitude = 18.5275,
                longitude = 73.8550,
                sequenceOrder = 5,
                scheduledTime = "09:30 AM",
                acceptedWasteTypes = setOf(WasteType.WET_ORGANIC, WasteType.DRY_RECYCLABLE),
                binCapacityKg = 180,
                status = PointStatus.PENDING
            ),
            CollectionPoint(
                id = "CP-06",
                villageId = "VIL-01",
                code = "SHV-06",
                name = "Gram Panchayat Bhavan & Hall",
                landmark = "Main administrative complex",
                type = PointType.GRAM_CHOWK,
                latitude = 18.5250,
                longitude = 73.8530,
                sequenceOrder = 6,
                scheduledTime = "10:00 AM",
                acceptedWasteTypes = setOf(WasteType.DRY_RECYCLABLE, WasteType.E_WASTE),
                binCapacityKg = 70,
                status = PointStatus.PENDING
            ),
            CollectionPoint(
                id = "CP-07",
                villageId = "VIL-01",
                code = "SHV-07",
                name = "Compost & Segregation Yard",
                landmark = "Village periphery road",
                type = PointType.PUBLIC_BIN,
                latitude = 18.5220,
                longitude = 73.8510,
                sequenceOrder = 7,
                scheduledTime = "10:30 AM",
                acceptedWasteTypes = setOf(WasteType.WET_ORGANIC),
                binCapacityKg = 250,
                status = PointStatus.PENDING
            ),

            // Rampur Khurd Points
            CollectionPoint(
                id = "CP-08",
                villageId = "VIL-02",
                code = "RMP-01",
                name = "Rampur Main Bus Stand",
                landmark = "Highway entry junction",
                type = PointType.PUBLIC_BIN,
                latitude = 18.5350,
                longitude = 73.8720,
                sequenceOrder = 1,
                scheduledTime = "07:45 AM",
                acceptedWasteTypes = setOf(WasteType.DRY_RECYCLABLE),
                binCapacityKg = 90,
                status = PointStatus.COMPLETED,
                completedAt = "07:50 AM",
                wetKgCollected = 12.0,
                dryKgCollected = 35.0,
                hazardousKgCollected = 0.0
            ),
            CollectionPoint(
                id = "CP-09",
                villageId = "VIL-02",
                code = "RMP-02",
                name = "Dairy Sahakari Society",
                landmark = "Near Milk Chilling Plant",
                type = PointType.GRAM_CHOWK,
                latitude = 18.5372,
                longitude = 73.8745,
                sequenceOrder = 2,
                scheduledTime = "08:15 AM",
                acceptedWasteTypes = setOf(WasteType.WET_ORGANIC),
                binCapacityKg = 100,
                status = PointStatus.PENDING
            ),
            CollectionPoint(
                id = "CP-10",
                villageId = "VIL-02",
                code = "RMP-03",
                name = "East Farmer Colony Cluster",
                landmark = "Wards 2-3 Boundary",
                type = PointType.HOUSE_CLUSTER,
                latitude = 18.5390,
                longitude = 73.8770,
                sequenceOrder = 3,
                scheduledTime = "08:45 AM",
                acceptedWasteTypes = setOf(WasteType.WET_ORGANIC, WasteType.DRY_RECYCLABLE),
                binCapacityKg = 110,
                status = PointStatus.PENDING
            )
        )

        val initialVans = listOf(
            Van(
                id = "VAN-01",
                regNumber = "MH-14-TR-4291",
                name = "Swachh Rath 01",
                model = "Tata Ace Gold EV",
                assignedDriverId = "STF-01",
                assignedVillageId = "VIL-01",
                capacityKg = 850,
                status = VanStatus.ON_ROUTE,
                currentLat = 18.5240,
                currentLng = 73.8605,
                speedKmH = 18.5f,
                headingDegrees = 42.0f,
                batteryOrFuelPercent = 78,
                odometerKm = 148.6,
                isGpsActive = true,
                lastPingTimestamp = "10 sec ago"
            ),
            Van(
                id = "VAN-02",
                regNumber = "MH-14-GH-1802",
                name = "Swachh Rath 02",
                model = "Mahindra Bolero Maxi Truck",
                assignedDriverId = "STF-02",
                assignedVillageId = "VIL-02",
                capacityKg = 1200,
                status = VanStatus.ON_ROUTE,
                currentLat = 18.5360,
                currentLng = 73.8730,
                speedKmH = 22.0f,
                headingDegrees = 90.0f,
                batteryOrFuelPercent = 64,
                odometerKm = 312.2,
                isGpsActive = true,
                lastPingTimestamp = "25 sec ago"
            ),
            Van(
                id = "VAN-03",
                regNumber = "MH-14-BC-9043",
                name = "Gram Seva 03",
                model = "Piaggio Ape Auto Plus",
                assignedDriverId = "STF-03",
                assignedVillageId = "VIL-03",
                capacityKg = 500,
                status = VanStatus.IDLE,
                currentLat = 18.5080,
                currentLng = 73.8340,
                speedKmH = 0.0f,
                headingDegrees = 0.0f,
                batteryOrFuelPercent = 95,
                odometerKm = 88.0,
                isGpsActive = true,
                lastPingTimestamp = "2 min ago"
            )
        )

        val initialRoutes = listOf(
            CollectionRoute(
                id = "RT-01",
                villageId = "VIL-01",
                title = "Shivaji Nagar Morning Circuit (Wards 1-4)",
                vanId = "VAN-01",
                driverId = "STF-01",
                startTime = "07:30 AM",
                estimatedDurationMins = 180,
                pointIds = listOf("CP-01", "CP-02", "CP-03", "CP-04", "CP-05", "CP-06", "CP-07"),
                isActive = true,
                currentPointIndex = 2
            ),
            CollectionRoute(
                id = "RT-02",
                villageId = "VIL-02",
                title = "Rampur Khurd Gram Circuit",
                vanId = "VAN-02",
                driverId = "STF-02",
                startTime = "07:45 AM",
                estimatedDurationMins = 120,
                pointIds = listOf("CP-08", "CP-09", "CP-10"),
                isActive = true,
                currentPointIndex = 1
            )
        )

        val initialLogs = listOf(
            CollectionLog(
                id = "LOG-01",
                timestamp = "07:38 AM",
                pointId = "CP-01",
                pointName = "North Gram Mandir Chowk",
                villageName = "Shivaji Nagar",
                vanReg = "MH-14-TR-4291",
                driverName = "Ramesh Patil",
                status = PointStatus.COMPLETED,
                totalWeightKg = 56.0,
                wetKg = 34.5,
                dryKg = 21.0,
                hazardousKg = 0.5,
                notes = "Bin emptied and area swept"
            ),
            CollectionLog(
                id = "LOG-02",
                timestamp = "08:09 AM",
                pointId = "CP-02",
                pointName = "Zilla Parishad Primary School",
                villageName = "Shivaji Nagar",
                vanReg = "MH-14-TR-4291",
                driverName = "Ramesh Patil",
                status = PointStatus.COMPLETED,
                totalWeightKg = 44.5,
                wetKg = 18.0,
                dryKg = 26.5,
                hazardousKg = 0.0,
                notes = "Midday meal organic bin emptied"
            ),
            CollectionLog(
                id = "LOG-03",
                timestamp = "07:50 AM",
                pointId = "CP-08",
                pointName = "Rampur Main Bus Stand",
                villageName = "Rampur Khurd",
                vanReg = "MH-14-GH-1802",
                driverName = "Suresh Pawar",
                status = PointStatus.COMPLETED,
                totalWeightKg = 47.0,
                wetKg = 12.0,
                dryKg = 35.0,
                hazardousKg = 0.0,
                notes = "Plastic & paper wrappers cleared"
            )
        )

        val initialAlerts = listOf(
            AlertNotification(
                id = "ALT-01",
                type = AlertType.LONG_IDLE,
                vanId = "VAN-01",
                vanName = "Swachh Rath 01",
                title = "Extended Halt Detected",
                message = "Van 01 has been stationary for 14 minutes near Kisan Basti.",
                timestamp = "08:42 AM",
                severity = AlertSeverity.WARNING,
                isResolved = false
            ),
            AlertNotification(
                id = "ALT-02",
                type = AlertType.OFF_ROUTE,
                vanId = "VAN-02",
                vanName = "Swachh Rath 02",
                title = "Route Deviation Warning",
                message = "Van 02 diverted 180m from scheduled lane to bypass road construction.",
                timestamp = "08:12 AM",
                severity = AlertSeverity.INFO,
                isResolved = true
            )
        )

        _villages.value = initialVillages
        _staffList.value = initialStaff
        _collectionPoints.value = initialPoints
        _vans.value = initialVans
        _routes.value = initialRoutes
        _collectionLogs.value = initialLogs
        _alerts.value = initialAlerts
    }

    // Action: Mark Point as Completed by Driver
    fun markPointCompleted(
        pointId: String,
        wetKg: Double,
        dryKg: Double,
        hazardousKg: Double,
        remarks: String?
    ) {
        val nowStr = timeFormat.format(Date())
        var completedPointName = ""
        var completedVillageId = ""

        _collectionPoints.update { list ->
            list.map { pt ->
                if (pt.id == pointId) {
                    completedPointName = pt.name
                    completedVillageId = pt.villageId
                    pt.copy(
                        status = PointStatus.COMPLETED,
                        completedAt = nowStr,
                        wetKgCollected = wetKg,
                        dryKgCollected = dryKg,
                        hazardousKgCollected = hazardousKg,
                        driverRemarks = remarks
                    )
                } else pt
            }
        }

        val activeVan = _vans.value.find { it.assignedVillageId == completedVillageId } ?: _vans.value.first()
        val driver = _staffList.value.find { it.id == activeVan.assignedDriverId }
        val village = _villages.value.find { it.id == completedVillageId }

        val newLog = CollectionLog(
            id = "LOG-${UUID.randomUUID().toString().take(6)}",
            timestamp = nowStr,
            pointId = pointId,
            pointName = completedPointName,
            villageName = village?.name ?: "Village",
            vanReg = activeVan.regNumber,
            driverName = driver?.name ?: "Driver",
            status = PointStatus.COMPLETED,
            totalWeightKg = wetKg + dryKg + hazardousKg,
            wetKg = wetKg,
            dryKg = dryKg,
            hazardousKg = hazardousKg,
            notes = remarks
        )

        _collectionLogs.update { listOf(newLog) + it }

        // Advance route pointer
        _routes.update { routeList ->
            routeList.map { r ->
                if (r.pointIds.contains(pointId)) {
                    val nextIdx = (r.currentPointIndex + 1).coerceAtMost(r.pointIds.size - 1)
                    val allDone = r.pointIds.all { pid ->
                        _collectionPoints.value.find { it.id == pid }?.status == PointStatus.COMPLETED
                    }
                    r.copy(
                        currentPointIndex = nextIdx,
                        isActive = !allDone
                    )
                } else r
            }
        }

        // Set next pending point to IN_PROGRESS
        val activeRoute = _routes.value.find { it.pointIds.contains(pointId) }
        activeRoute?.pointIds?.firstOrNull { pid ->
            val p = _collectionPoints.value.find { it.id == pid }
            p?.status == PointStatus.PENDING
        }?.let { nextPid ->
            _collectionPoints.update { list ->
                list.map { if (it.id == nextPid) it.copy(status = PointStatus.IN_PROGRESS) else it }
            }
        }
    }

    // Action: Mark Point as Missed
    fun markPointMissed(
        pointId: String,
        reason: String,
        remarks: String?
    ) {
        val nowStr = timeFormat.format(Date())
        var missedPointName = ""
        var missedVillageId = ""

        _collectionPoints.update { list ->
            list.map { pt ->
                if (pt.id == pointId) {
                    missedPointName = pt.name
                    missedVillageId = pt.villageId
                    pt.copy(
                        status = PointStatus.MISSED,
                        completedAt = nowStr,
                        missedReason = reason,
                        driverRemarks = remarks
                    )
                } else pt
            }
        }

        val activeVan = _vans.value.find { it.assignedVillageId == missedVillageId } ?: _vans.value.first()
        val driver = _staffList.value.find { it.id == activeVan.assignedDriverId }
        val village = _villages.value.find { it.id == missedVillageId }

        val newLog = CollectionLog(
            id = "LOG-${UUID.randomUUID().toString().take(6)}",
            timestamp = nowStr,
            pointId = pointId,
            pointName = missedPointName,
            villageName = village?.name ?: "Village",
            vanReg = activeVan.regNumber,
            driverName = driver?.name ?: "Driver",
            status = PointStatus.MISSED,
            totalWeightKg = 0.0,
            wetKg = 0.0,
            dryKg = 0.0,
            hazardousKg = 0.0,
            notes = "Missed: $reason. ${remarks ?: ""}"
        )

        _collectionLogs.update { listOf(newLog) + it }

        // Trigger Alert for Missed Point
        val alert = AlertNotification(
            id = "ALT-${UUID.randomUUID().toString().take(6)}",
            type = AlertType.MISSED_POINT,
            vanId = activeVan.id,
            vanName = activeVan.name,
            title = "Collection Point Skipped",
            message = "$missedPointName was missed by ${activeVan.name}. Reason: $reason",
            timestamp = nowStr,
            severity = AlertSeverity.WARNING,
            isResolved = false
        )
        _alerts.update { listOf(alert) + it }

        // Advance route index
        _routes.update { routeList ->
            routeList.map { r ->
                if (r.pointIds.contains(pointId)) {
                    val nextIdx = (r.currentPointIndex + 1).coerceAtMost(r.pointIds.size - 1)
                    r.copy(currentPointIndex = nextIdx)
                } else r
            }
        }
    }

    // Trigger Emergency SOS / Breakdown by Driver
    fun triggerBreakdownSos(vanId: String, issueType: String, notes: String?) {
        val nowStr = timeFormat.format(Date())
        val van = _vans.value.find { it.id == vanId } ?: return

        _vans.update { list ->
            list.map {
                if (it.id == vanId) it.copy(status = VanStatus.BREAKDOWN_SOS, speedKmH = 0f) else it
            }
        }

        val alert = AlertNotification(
            id = "ALT-${UUID.randomUUID().toString().take(6)}",
            type = AlertType.BREAKDOWN_SOS,
            vanId = van.id,
            vanName = van.name,
            title = "EMERGENCY: ${issueType.uppercase()}",
            message = "${van.name} (${van.regNumber}) reported an emergency: $issueType. ${notes ?: ""}",
            timestamp = nowStr,
            severity = AlertSeverity.CRITICAL,
            isResolved = false
        )
        _alerts.update { listOf(alert) + it }
    }

    // Resolve an Alert
    fun resolveAlert(alertId: String) {
        _alerts.update { list ->
            list.map {
                if (it.id == alertId) it.copy(isResolved = true) else it
            }
        }
    }

    // Add New Collection Point
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
        val villagePoints = _collectionPoints.value.filter { it.villageId == villageId }
        val nextSeq = (villagePoints.maxOfOrNull { it.sequenceOrder } ?: 0) + 1
        val villagePrefix = _villages.value.find { it.id == villageId }?.name?.take(3)?.uppercase() ?: "PTS"
        val code = "$villagePrefix-0$nextSeq"

        val newPoint = CollectionPoint(
            id = "CP-${UUID.randomUUID().toString().take(6)}",
            villageId = villageId,
            code = code,
            name = name,
            landmark = landmark,
            type = type,
            latitude = latitude,
            longitude = longitude,
            sequenceOrder = nextSeq,
            scheduledTime = scheduledTime,
            acceptedWasteTypes = acceptedWasteTypes,
            binCapacityKg = capacityKg,
            status = PointStatus.PENDING
        )

        _collectionPoints.update { it + newPoint }

        // Also add point to the village's active route if any
        _routes.update { routeList ->
            routeList.map { r ->
                if (r.villageId == villageId) {
                    r.copy(pointIds = r.pointIds + newPoint.id)
                } else r
            }
        }
    }

    // Add New Van
    fun addVan(
        regNumber: String,
        name: String,
        model: String,
        assignedDriverId: String,
        assignedVillageId: String,
        capacityKg: Int
    ) {
        val village = _villages.value.find { it.id == assignedVillageId }
        val newVan = Van(
            id = "VAN-${UUID.randomUUID().toString().take(6)}",
            regNumber = regNumber,
            name = name,
            model = model,
            assignedDriverId = assignedDriverId,
            assignedVillageId = assignedVillageId,
            capacityKg = capacityKg,
            status = VanStatus.IDLE,
            currentLat = village?.centerLat ?: 18.5204,
            currentLng = village?.centerLng ?: 73.8567,
            speedKmH = 0f,
            headingDegrees = 0f,
            batteryOrFuelPercent = 100,
            odometerKm = 0.0,
            isGpsActive = true,
            lastPingTimestamp = "Just now"
        )
        _vans.update { it + newVan }

        // Link driver
        _staffList.update { list ->
            list.map {
                if (it.id == assignedDriverId) it.copy(assignedVanId = newVan.id) else it
            }
        }
    }

    // Add New Staff
    fun addStaff(
        name: String,
        role: StaffRole,
        phone: String,
        assignedVanId: String?
    ) {
        val newStaff = Staff(
            id = "STF-${UUID.randomUUID().toString().take(6)}",
            name = name,
            role = role,
            phone = phone,
            assignedVanId = assignedVanId,
            attendanceStatus = AttendanceStatus.ON_DUTY,
            shiftsCompleted = 0
        )
        _staffList.update { it + newStaff }
    }

    // Add New Village
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
        val newVillage = Village(
            id = "VIL-${UUID.randomUUID().toString().take(6)}",
            name = name,
            gramPanchayat = gramPanchayat,
            blockTaluka = blockTaluka,
            district = district,
            totalWards = totalWards,
            population = population,
            sarpanchName = sarpanchName,
            supervisorPhone = supervisorPhone,
            centerLat = centerLat,
            centerLng = centerLng
        )
        _villages.update { it + newVillage }
    }

    // Telemetry updates (Real GPS or Simulation)
    fun updateVanTelemetry(
        vanId: String,
        lat: Double,
        lng: Double,
        speedKmH: Float,
        heading: Float,
        distanceIncrementKm: Double = 0.02
    ) {
        _vans.update { list ->
            list.map {
                if (it.id == vanId) {
                    it.copy(
                        currentLat = lat,
                        currentLng = lng,
                        speedKmH = speedKmH,
                        headingDegrees = heading,
                        odometerKm = (it.odometerKm + distanceIncrementKm).let { d -> (d * 10).toInt() / 10.0 },
                        batteryOrFuelPercent = (it.batteryOrFuelPercent - 0.01).coerceAtLeast(10.0).toInt(),
                        lastPingTimestamp = "Just now"
                    )
                } else it
            }
        }
    }

    // GPS Simulation Step along Route Points
    fun stepSimulation(routeId: String) {
        val route = _routes.value.find { it.id == routeId } ?: return
        val van = _vans.value.find { it.id == route.vanId } ?: return
        val points = route.pointIds.mapNotNull { pid -> _collectionPoints.value.find { it.id == pid } }
        if (points.isEmpty()) return

        // Target next pending or in_progress point
        val targetPoint = points.getOrNull(route.currentPointIndex) ?: points.last()

        val dLat = targetPoint.latitude - van.currentLat
        val dLng = targetPoint.longitude - van.currentLng
        val distance = Math.hypot(dLat, dLng)

        if (distance < 0.0003) {
            // Reached target point!
            updateVanTelemetry(
                vanId = van.id,
                lat = targetPoint.latitude,
                lng = targetPoint.longitude,
                speedKmH = 0f,
                heading = van.headingDegrees
            )
            // If current point was pending, mark IN_PROGRESS
            if (targetPoint.status == PointStatus.PENDING) {
                _collectionPoints.update { list ->
                    list.map { if (it.id == targetPoint.id) it.copy(status = PointStatus.IN_PROGRESS) else it }
                }
            }
        } else {
            // Move fraction towards point
            val stepFraction = (0.08 * _simulationSpeedMultiplier.value).coerceAtMost(0.5)
            val newLat = van.currentLat + dLat * stepFraction
            val newLng = van.currentLng + dLng * stepFraction
            val heading = (Math.toDegrees(Math.atan2(dLng, dLat)).toFloat() + 360f) % 360f
            val simulatedSpeed = (18f + (Math.random() * 8).toFloat()) * _simulationSpeedMultiplier.value

            updateVanTelemetry(
                vanId = van.id,
                lat = newLat,
                lng = newLng,
                speedKmH = simulatedSpeed,
                heading = heading,
                distanceIncrementKm = 0.04 * _simulationSpeedMultiplier.value
            )
        }
    }
}
