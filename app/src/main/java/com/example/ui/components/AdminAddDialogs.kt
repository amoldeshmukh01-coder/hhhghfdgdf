package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCollectionPointDialog(
    villages: List<Village>,
    selectedVillageId: String,
    onDismiss: () -> Unit,
    onConfirm: (
        villageId: String,
        name: String,
        landmark: String,
        type: PointType,
        latitude: Double,
        longitude: Double,
        capacityKg: Int,
        scheduledTime: String,
        acceptedWasteTypes: Set<WasteType>
    ) -> Unit
) {
    var vId by remember { mutableStateOf(selectedVillageId) }
    var name by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PointType.PUBLIC_BIN) }
    var latText by remember { mutableStateOf("18.5230") }
    var lngText by remember { mutableStateOf("73.8570") }
    var capacityText by remember { mutableStateOf("80") }
    var timeText by remember { mutableStateOf("08:00 AM") }
    var isWetSelected by remember { mutableStateOf(true) }
    var isDrySelected by remember { mutableStateOf(true) }
    var isHazardousSelected by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Collection Point", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Point Name (e.g. Zilla Parishad School Bin)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_point_name")
                )

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Landmark / Ward") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Point Type dropdown/radio
                Text("Point Type:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                PointType.values().forEach { type ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Text(text = type.label, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Latitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("Longitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = capacityText,
                        onValueChange = { capacityText = it },
                        label = { Text("Bin Capacity (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text("Schedule Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Accepted Waste Categories:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = isWetSelected, onCheckedChange = { isWetSelected = it })
                    Text("Wet Organic", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(12.dp))
                    Checkbox(checked = isDrySelected, onCheckedChange = { isDrySelected = it })
                    Text("Dry Recyclable", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = isHazardousSelected, onCheckedChange = { isHazardousSelected = it })
                    Text("Hazardous / Sanitary", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull() ?: 18.520
                    val lng = lngText.toDoubleOrNull() ?: 73.856
                    val cap = capacityText.toIntOrNull() ?: 60
                    val types = mutableSetOf<WasteType>()
                    if (isWetSelected) types.add(WasteType.WET_ORGANIC)
                    if (isDrySelected) types.add(WasteType.DRY_RECYCLABLE)
                    if (isHazardousSelected) types.add(WasteType.HAZARDOUS_SANITARY)
                    onConfirm(vId, name.ifBlank { "Collection Point" }, landmark, selectedType, lat, lng, cap, timeText, types)
                },
                modifier = Modifier.testTag("btn_save_point")
            ) {
                Text("Register Point")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddVanDialog(
    drivers: List<Staff>,
    villages: List<Village>,
    onDismiss: () -> Unit,
    onConfirm: (regNumber: String, name: String, model: String, driverId: String, villageId: String, capacityKg: Int) -> Unit
) {
    var regNumber by remember { mutableStateOf("MH-14-") }
    var name by remember { mutableStateOf("Swachh Rath") }
    var model by remember { mutableStateOf("Tata Ace Gold EV") }
    var selectedDriverId by remember { mutableStateOf(drivers.firstOrNull()?.id ?: "") }
    var selectedVillageId by remember { mutableStateOf(villages.firstOrNull()?.id ?: "") }
    var capacityText by remember { mutableStateOf("900") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Collection Van", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = regNumber,
                    onValueChange = { regNumber = it },
                    label = { Text("Vehicle Reg Number (e.g. MH-14-TR-5050)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Van Fleet Name (e.g. Swachh Rath 04)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Vehicle Make & Model") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = capacityText,
                    onValueChange = { capacityText = it },
                    label = { Text("Payload Capacity (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Assign Village:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                villages.forEach { v ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedVillageId == v.id,
                            onClick = { selectedVillageId = v.id }
                        )
                        Text(text = v.name, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Text("Assign Driver:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                drivers.filter { it.role == StaffRole.DRIVER }.forEach { d ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDriverId == d.id,
                            onClick = { selectedDriverId = d.id }
                        )
                        Text(text = "${d.name} (${d.phone})", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacityText.toIntOrNull() ?: 800
                    onConfirm(regNumber, name, model, selectedDriverId, selectedVillageId, cap)
                }
            ) {
                Text("Add Van")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddStaffDialog(
    vans: List<Van>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, role: StaffRole, phone: String, assignedVanId: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+91 ") }
    var selectedRole by remember { mutableStateOf(StaffRole.DRIVER) }
    var selectedVanId by remember { mutableStateOf(vans.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Sanitation Staff / Driver", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Role:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                StaffRole.values().forEach { role ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role }
                        )
                        Text(text = role.label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedRole, phone, selectedVanId)
                    }
                }
            ) {
                Text("Register Staff")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddVillageDialog(
    onDismiss: () -> Unit,
    onConfirm: (
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
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var taluka by remember { mutableStateOf("Haveli") }
    var district by remember { mutableStateOf("Pune") }
    var wardsText by remember { mutableStateOf("3") }
    var popText by remember { mutableStateOf("2500") }
    var sarpanch by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+91 ") }
    var latText by remember { mutableStateOf("18.5200") }
    var lngText by remember { mutableStateOf("73.8500") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Village / Gram Panchayat", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Village Name (e.g. Chandoli)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = taluka,
                        onValueChange = { taluka = it },
                        label = { Text("Block / Taluka") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("District") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = wardsText,
                        onValueChange = { wardsText = it },
                        label = { Text("Wards") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = popText,
                        onValueChange = { popText = it },
                        label = { Text("Population") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = sarpanch,
                    onValueChange = { sarpanch = it },
                    label = { Text("Sarpanch / Village Head Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Panchayat Supervisor Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val w = wardsText.toIntOrNull() ?: 3
                        val p = popText.toIntOrNull() ?: 2000
                        val lat = latText.toDoubleOrNull() ?: 18.520
                        val lng = lngText.toDoubleOrNull() ?: 73.850
                        onConfirm(name, "$name Gram Panchayat", taluka, district, w, p, sarpanch, phone, lat, lng)
                    }
                }
            ) {
                Text("Register Village")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
