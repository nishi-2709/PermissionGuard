package com.permissionguard.domain.model

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class AppInfo(
    val appName: String,
    val packageName: String,
    val totalPermissions: Int,
    val dangerousPermissions: List<String>,
    val riskLevel: RiskLevel,
    val riskReason: String,
    val isSideloaded: Boolean = false,
    val icon: android.graphics.Bitmap? = null
)
