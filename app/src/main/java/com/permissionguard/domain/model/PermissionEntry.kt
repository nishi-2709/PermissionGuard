package com.permissionguard.domain.model

data class PermissionEntry(
    val id: String, // e.g., android.permission.CAMERA
    val name: String, // e.g., Camera
    val description: String, // Plain English description
    val riskLevel: RiskLevel
)
