package com.permissionguard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.permissionguard.di.AppContainer
import com.permissionguard.ui.appdetail.AppDetailViewModel
import com.permissionguard.ui.apps.AppsViewModel
import com.permissionguard.ui.dashboard.DashboardViewModel
import com.permissionguard.ui.monitor.MonitorViewModel

class ViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppsViewModel(appContainer.appScanner) as T
        }
        if (modelClass.isAssignableFrom(MonitorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MonitorViewModel(appContainer.permissionRepository) as T
        }
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(appContainer.appScanner, appContainer.permissionRepository) as T
        }
        if (modelClass.isAssignableFrom(AppDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppDetailViewModel(appContainer.appScanner) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
