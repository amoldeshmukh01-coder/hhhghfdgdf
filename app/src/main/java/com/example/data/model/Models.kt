package com.example.data.model

enum class UserRole {
    ADMIN_SUPERVISOR,
    DRIVER_FIELD
}

enum class PointType(val label: String, val iconName: String) {
    PUBLIC_BIN("Public Community Bin", "Delete"),
    HOUSE_CLUSTER("Residential Cluster", "Home"),
    GRAM_CHOWK("Gram Panchayat Chowk", "AccountBalance"),
    PRIMARY_SCHOOL("Village School / Anganwadi", "School"),
    HEALTH_SUB_CENTRE("Primary Health Centre", "LocalHospital"),
    MARKET_AREA("Weekly Haat / Bazaar", "Storefront")
}

enum class PointStatus(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("Approaching"),
    COMPLETED("Cleaned & Collected"),
    MISSED("Missed / Inaccessible")
}

enum class WasteType(val label: String) {
    WET_ORGANIC("Wet (Organic/Kitchen)"),
    DRY_RECYCLABLE("Dry (Plastic/Paper)"),
    HAZARDOUS_SANITARY("Hazardous / Sanitary"),
    E_WASTE("E-Waste / Metal")
}

data class Village(
    val id: String,
    val name: String,
    val gramPanchayat: String,
    val blockTaluka: String,
    val district: String,
    val totalWards: Int,
    val population: Int,
    val sarpanchName: String,
    val supervisorPhone: String,
    val centerLat: Double,
    val centerLng: Double
)

data class CollectionPoint(
    val id: String,
    val villageId: String,
    val code: String, // e.g., "SHV-01"
    val name: String,
    val landmark: String,
    val type: PointType,
    val latitude: Double,
    val longitude: Double,
    val sequenceOrder: Int,
    val scheduledTime: String,
    val acceptedWasteTypes: Set<WasteType>,
    val binCapacityKg: Int,
    val status: PointStatus = PointStatus.PENDING,
    val completedAt: String? = null,
    val wetKgCollected: Double = 0.0,
    val dryKgCollected: Double = 0.0,
    val hazardousKgCollected: Double = 0.0,
    val missedReason: String? = null,
    val driverRemarks: String? = null
)

enum class VanStatus(val label: String) {
    ON_ROUTE("On Collection Route"),
    IDLE("Stationed / Standby"),
    COMPLETED("Daily Route Completed"),
    BREAKDOWN_SOS("Breakdown / Alert")
}

data class Van(
    val id: String,
    val regNumber: String,
    val name: String,
    val model: String,
    val assignedDriverId: String,
    val assignedVillageId: String,
    val capacityKg: Int,
    val status: VanStatus = VanStatus.IDLE,
    val currentLat: Double,
    val currentLng: Double,
    val speedKmH: Float = 0f,
    val headingDegrees: Float = 0f,
    val batteryOrFuelPercent: Int = 85,
    val odometerKm: Double = 142.4,
    val isGpsActive: Boolean = true,
    val lastPingTimestamp: String = "Just now"
)

enum class StaffRole(val label: String) {
    DRIVER("Van Driver"),
    SANITATION_WORKER("Safai Karamchari"),
    ROUTE_SUPERVISOR("Gram Panchayat Supervisor")
}

enum class AttendanceStatus(val label: String) {
    ON_DUTY("On Duty"),
    PRESENT("Present"),
    OFF_DUTY("Off Duty")
}

data class Staff(
    val id: String,
    val name: String,
    val role: StaffRole,
    val phone: String,
    val assignedVanId: String?,
    val attendanceStatus: AttendanceStatus = AttendanceStatus.ON_DUTY,
    val shiftsCompleted: Int = 24
)

data class CollectionRoute(
    val id: String,
    val villageId: String,
    val title: String,
    val vanId: String,
    val driverId: String,
    val startTime: String,
    val estimatedDurationMins: Int,
    val pointIds: List<String>,
    val isActive: Boolean = false,
    val currentPointIndex: Int = 0
)

data class CollectionLog(
    val id: String,
    val timestamp: String,
    val pointId: String,
    val pointName: String,
    val villageName: String,
    val vanReg: String,
    val driverName: String,
    val status: PointStatus,
    val totalWeightKg: Double,
    val wetKg: Double,
    val dryKg: Double,
    val hazardousKg: Double,
    val notes: String?
)

enum class AlertSeverity(val label: String) {
    CRITICAL("Critical Alert"),
    WARNING("Warning"),
    INFO("Information")
}

enum class AlertType {
    OFF_ROUTE,
    LONG_IDLE,
    MISSED_POINT,
    BREAKDOWN_SOS,
    ROUTE_FINISHED
}

data class AlertNotification(
    val id: String,
    val type: AlertType,
    val vanId: String,
    val vanName: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val severity: AlertSeverity,
    val isResolved: Boolean = false
)
