package com.permissionguard.utils

import com.permissionguard.domain.model.PermissionEntry
import com.permissionguard.domain.model.RiskLevel

object PermissionDatabase {
    val allPermissions = listOf(
        PermissionEntry(
            id = "android.permission.CAMERA",
            name = "Camera",
            description = "Allows the app to take pictures and record videos using the device's camera. This can happen in the background on older Android versions.",
            riskLevel = RiskLevel.HIGH
        ),
        PermissionEntry(
            id = "android.permission.RECORD_AUDIO",
            name = "Microphone",
            description = "Allows the app to record audio using the microphone. Malicious apps could use this to eavesdrop on conversations.",
            riskLevel = RiskLevel.HIGH
        ),
        PermissionEntry(
            id = "android.permission.ACCESS_FINE_LOCATION",
            name = "Precise Location",
            description = "Allows the app to get your exact location using GPS. This can be used to track your physical movements.",
            riskLevel = RiskLevel.HIGH
        ),
        PermissionEntry(
            id = "android.permission.ACCESS_COARSE_LOCATION",
            name = "Approximate Location",
            description = "Allows the app to get your general location using Wi-Fi and cellular networks.",
            riskLevel = RiskLevel.MEDIUM
        ),
        PermissionEntry(
            id = "android.permission.ACCESS_BACKGROUND_LOCATION",
            name = "Background Location",
            description = "Allows the app to track your location constantly, even when you are not actively using the app. High risk for stalking.",
            riskLevel = RiskLevel.HIGH
        ),
        PermissionEntry(
            id = "android.permission.READ_CONTACTS",
            name = "Read Contacts",
            description = "Allows the app to read all contact information stored on your phone, including names, numbers, and emails.",
            riskLevel = RiskLevel.MEDIUM
        ),
        PermissionEntry(
            id = "android.permission.READ_SMS",
            name = "Read SMS",
            description = "Allows the app to read your text messages. This is extremely dangerous as it can be used to steal banking OTP codes.",
            riskLevel = RiskLevel.HIGH
        ),
        PermissionEntry(
            id = "android.permission.SEND_SMS",
            name = "Send SMS",
            description = "Allows the app to send text messages, potentially costing you money or messaging premium-rate numbers.",
            riskLevel = RiskLevel.HIGH
        ),
        PermissionEntry(
            id = "android.permission.READ_CALL_LOG",
            name = "Read Call Logs",
            description = "Allows the app to see who you called, who called you, and when. Exposes your social circle.",
            riskLevel = RiskLevel.HIGH
        ),
        PermissionEntry(
            id = "android.permission.READ_EXTERNAL_STORAGE",
            name = "Read Storage",
            description = "Allows the app to read files, photos, and media stored on your device.",
            riskLevel = RiskLevel.MEDIUM
        ),
        PermissionEntry(
            id = "android.permission.INTERNET",
            name = "Internet",
            description = "Allows the app to open network sockets to access the internet. Almost all apps have this.",
            riskLevel = RiskLevel.LOW
        ),
        PermissionEntry(
            id = "android.permission.BLUETOOTH",
            name = "Bluetooth",
            description = "Allows the app to connect to paired Bluetooth devices.",
            riskLevel = RiskLevel.LOW
        )
    ).sortedBy { it.name }
}
