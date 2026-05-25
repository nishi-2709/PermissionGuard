package com.permissionguard.ui.appdetail

import androidx.lifecycle.ViewModel
import com.permissionguard.domain.model.AppInfo
import com.permissionguard.domain.model.PermissionEntry
import com.permissionguard.domain.scanner.AppScanner
import com.permissionguard.utils.PermissionDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppDetailViewModel(private val appScanner: AppScanner) : ViewModel() {

    private val _appInfo = MutableStateFlow<AppInfo?>(null)
    val appInfo: StateFlow<AppInfo?> = _appInfo.asStateFlow()

    private val _permissionDetails = MutableStateFlow<List<PermissionEntry>>(emptyList())
    val permissionDetails: StateFlow<List<PermissionEntry>> = _permissionDetails.asStateFlow()

    fun loadAppDetails(packageName: String) {
        val info = appScanner.getAppInfo(packageName)
        _appInfo.value = info
        
        if (info != null) {
            val details = info.dangerousPermissions.mapNotNull { permName ->
                PermissionDatabase.allPermissions.find { it.id == permName } ?: 
                PermissionEntry(
                    id = permName,
                    name = permName.substringAfterLast("."),
                    description = "This permission allows the app to access specific device data or features.",
                    riskLevel = com.permissionguard.domain.model.RiskLevel.LOW
                )
            }
            _permissionDetails.value = details
        }
    }
}
