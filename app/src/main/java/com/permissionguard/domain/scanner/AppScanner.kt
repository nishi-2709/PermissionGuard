package com.permissionguard.domain.scanner

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import com.permissionguard.domain.model.AppInfo
import com.permissionguard.domain.model.RiskLevel

class AppScanner(private val context: Context) {

    private var cachedApps: List<AppInfo>? = null
    private var lastFingerprint: Long = 0L

    private fun getAppsFingerprint(packages: List<android.content.pm.PackageInfo>): Long {
        var fingerprint = 0L
        for (pkg in packages) {
            fingerprint += pkg.lastUpdateTime
        }
        return fingerprint
    }

    fun clearCache() {
        cachedApps = null
    }

    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        
        val currentFingerprint = getAppsFingerprint(packages)
        
        if (cachedApps != null && currentFingerprint == lastFingerprint) {
            return cachedApps!!
        }
        
        val appList = mutableListOf<AppInfo>()

        // Comprehensive dangerous permissions list
        val targetPermissions = setOf(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_BACKGROUND_LOCATION",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.READ_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.SEND_SMS",
            "android.permission.READ_CALL_LOG",
            "android.permission.WRITE_CALL_LOG"
        )

        for (packageInfo in packages) {
            // Skip system apps based on flags
            if ((packageInfo.applicationInfo?.flags ?: 0) and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0) {
                continue
            }

            val appName = packageInfo.applicationInfo?.loadLabel(packageManager)?.toString() ?: packageInfo.packageName
            val requestedPermissions = packageInfo.requestedPermissions ?: emptyArray()
            
            val dangerousRequested = requestedPermissions.filter { targetPermissions.contains(it) }
            
            val installSource = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    packageManager.getInstallSourceInfo(packageInfo.packageName).installingPackageName
                } catch (e: Exception) {
                    null
                }
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageInfo.packageName)
            }
            
            val isFromPlayStore = installSource == "com.android.vending"
            val isFromKnownStore = installSource == "com.sec.android.app.samsungapps" || installSource == "com.amazon.venezia" || installSource == "com.heytap.market"
            val isSideloaded = !isFromPlayStore && !isFromKnownStore && installSource != null
            
            val riskLevel = calculateRiskLevel(dangerousRequested, installSource)
            val riskReason = generateRiskReason(dangerousRequested)
            
            var iconBitmap: android.graphics.Bitmap? = null
            try {
                val iconDrawable = packageInfo.applicationInfo?.loadIcon(packageManager)
                iconBitmap = iconDrawable?.toBitmap(128, 128)
            } catch (e: Exception) {
                // Ignore icon loading failures
            }

            appList.add(
                AppInfo(
                    appName = appName,
                    packageName = packageInfo.packageName,
                    totalPermissions = requestedPermissions.size,
                    dangerousPermissions = dangerousRequested,
                    riskLevel = riskLevel,
                    riskReason = riskReason,
                    isSideloaded = isSideloaded,
                    icon = iconBitmap
                )
            )
        }
        
        val finalSortedList = appList.sortedByDescending { it.riskLevel }
        cachedApps = finalSortedList
        lastFingerprint = currentFingerprint
        
        return finalSortedList
    }

    fun getAppInfo(packageName: String): AppInfo? {
        if (cachedApps == null) {
            getInstalledApps()
        }
        return cachedApps?.find { it.packageName == packageName }
    }

    private fun calculateRiskLevel(dangerousPermissions: List<String>, installSource: String?): RiskLevel {
        var score = 0
        
        // 1 Point (Standard)
        if (dangerousPermissions.any { it.contains("STORAGE") || it.contains("MEDIA") }) score += 1
        if (dangerousPermissions.any { it.contains("CONTACTS") }) score += 1
        if (dangerousPermissions.contains("android.permission.ACCESS_COARSE_LOCATION") || 
            dangerousPermissions.contains("android.permission.ACCESS_FINE_LOCATION")) score += 1
            
        // 2 Points (Sensitive)
        if (dangerousPermissions.contains("android.permission.CAMERA")) score += 2
        if (dangerousPermissions.contains("android.permission.RECORD_AUDIO")) score += 2
        
        // 3 Points (High Abuse)
        if (dangerousPermissions.contains("android.permission.ACCESS_BACKGROUND_LOCATION")) score += 3
        
        // 4 Points (Severe Risk)
        if (dangerousPermissions.any { it.contains("SMS") }) score += 4
        if (dangerousPermissions.any { it.contains("CALL_LOG") }) score += 4

        // Sideloaded Penalty (+2 points instead of 4 to prevent over-penalizing)
        val isFromPlayStore = installSource == "com.android.vending"
        val isFromKnownStore = installSource == "com.sec.android.app.samsungapps" || installSource == "com.amazon.venezia" || installSource == "com.heytap.market"
        
        // If it's not from a recognized safe store, add a minor penalty
        // We only penalize if it's explicitly not from a safe store, and we don't penalize ADB installs (null) as harshly
        if (!isFromPlayStore && !isFromKnownStore && installSource != null) {
            score += 2
        }

        return when {
            score >= 9 -> RiskLevel.HIGH
            score in 5..8 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }

    private fun generateRiskReason(dangerousPermissions: List<String>): String {
        if (dangerousPermissions.isEmpty()) return "Safe"
        
        val reasons = mutableListOf<String>()
        if (dangerousPermissions.contains("android.permission.CAMERA")) reasons.add("Camera")
        if (dangerousPermissions.contains("android.permission.RECORD_AUDIO")) reasons.add("Mic")
        if (dangerousPermissions.any { it.contains("LOCATION") }) reasons.add("Location")
        if (dangerousPermissions.any { it.contains("CONTACTS") }) reasons.add("Contacts")
        if (dangerousPermissions.any { it.contains("STORAGE") || it.contains("MEDIA") }) reasons.add("Storage/Media")
        if (dangerousPermissions.any { it.contains("SMS") }) reasons.add("SMS")
        if (dangerousPermissions.any { it.contains("CALL_LOG") }) reasons.add("Call Logs")
        
        if (reasons.isEmpty()) return "Uses dangerous permissions"
        
        return "Uses " + reasons.joinToString(" + ")
    }
}
