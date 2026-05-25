package com.permissionguard

import android.app.Application
import com.permissionguard.di.AppContainer

class PermissionGuardApplication : Application() {
    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
