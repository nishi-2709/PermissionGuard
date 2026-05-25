package com.permissionguard.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Apps : Screen("apps", "Apps", Icons.Default.List)
    object Monitor : Screen("monitor", "Monitor", Icons.Default.Warning)
    
    object AppDetail {
        const val route = "app_detail/{packageName}"
        fun createRoute(packageName: String) = "app_detail/$packageName"
    }
}
