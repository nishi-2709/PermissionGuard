package com.permissionguard.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "permission_events")
data class PermissionEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val permissionType: String, // e.g., "CAMERA", "MICROPHONE"
    val timestamp: Long,
    val isMock: Boolean = false
)
