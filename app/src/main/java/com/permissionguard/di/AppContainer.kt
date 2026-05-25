package com.permissionguard.di

import android.content.Context
import com.permissionguard.data.local.AppDatabase
import com.permissionguard.data.repository.PermissionRepository
import com.permissionguard.domain.scanner.AppScanner

class AppContainer(private val context: Context) {
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    
    val permissionRepository: PermissionRepository by lazy {
        PermissionRepository(database.permissionEventDao())
    }
    
    val appScanner: AppScanner by lazy {
        AppScanner(context)
    }
}
