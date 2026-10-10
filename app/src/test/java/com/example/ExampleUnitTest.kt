package com.example

import com.example.data.model.PointStatus
import com.example.data.model.PointType
import com.example.data.model.WasteType
import com.example.data.repository.FleetRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testInitialFleetRepositoryState() {
    val repository = FleetRepository.getInstance()
    assertTrue(repository.villages.value.isNotEmpty())
    assertTrue(repository.vans.value.isNotEmpty())
    assertTrue(repository.collectionPoints.value.isNotEmpty())
    assertTrue(repository.staffList.value.isNotEmpty())
  }

  @Test
  fun testMarkPointCompleted() {
    val repository = FleetRepository.getInstance()
    val point = repository.collectionPoints.value.first()
    repository.markPointCompleted(
      pointId = point.id,
      wetKg = 20.0,
      dryKg = 15.0,
      hazardousKg = 1.0,
      remarks = "Cleaned by van driver"
    )
    val updated = repository.collectionPoints.value.find { it.id == point.id }
    assertNotNull(updated)
    assertEquals(PointStatus.COMPLETED, updated?.status)
    assertEquals(20.0, updated?.wetKgCollected ?: 0.0, 0.01)
  }

  @Test
  fun testAddCollectionPoint() {
    val repository = FleetRepository.getInstance()
    val initialSize = repository.collectionPoints.value.size
    repository.addCollectionPoint(
      villageId = "VIL-01",
      name = "New Test Bin",
      landmark = "Near Well",
      type = PointType.PUBLIC_BIN,
      latitude = 18.525,
      longitude = 73.858,
      capacityKg = 75,
      scheduledTime = "09:00 AM",
      acceptedWasteTypes = setOf(WasteType.WET_ORGANIC)
    )
    assertEquals(initialSize + 1, repository.collectionPoints.value.size)
  }
}
