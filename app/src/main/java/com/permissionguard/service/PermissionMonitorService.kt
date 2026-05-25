package com.permissionguard.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.pm.PackageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.core.app.NotificationCompat
import com.permissionguard.data.local.AppDatabase
import com.permissionguard.data.repository.PermissionRepository
import com.permissionguard.domain.model.PermissionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PermissionMonitorService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    private lateinit var repository: PermissionRepository
    
    private val CHANNEL_ID = "PermissionMonitorChannel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        val dao = AppDatabase.getDatabase(applicationContext).permissionEventDao()
        repository = PermissionRepository(dao)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PermissionGuard Active")
            .setContentText("Monitoring background activity.")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setOngoing(true)
            .build()
            
        startForeground(NOTIFICATION_ID, notification)
        startMonitoring()
        
        return START_STICKY
    }

    private fun startMonitoring() {
        serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val pm = packageManager
            var lastPackageName = ""

            while (isActive) {
                val time = System.currentTimeMillis()
                val events = usageStatsManager.queryEvents(time - 5000, time)
                var currentPackage = lastPackageName
                val event = UsageEvents.Event()
                
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        currentPackage = event.packageName
                    }
                }

                if (currentPackage.isNotEmpty() && currentPackage != lastPackageName && currentPackage != packageName) {
                    lastPackageName = currentPackage
                    
                    try {
                        val packageInfo = pm.getPackageInfo(currentPackage, PackageManager.GET_PERMISSIONS)
                        val requestedPermissions = packageInfo.requestedPermissions ?: emptyArray()
                        
                        val hasCamera = requestedPermissions.contains(android.Manifest.permission.CAMERA)
                        val hasMic = requestedPermissions.contains(android.Manifest.permission.RECORD_AUDIO)
                        
                        val now = System.currentTimeMillis()
                        
                        if (hasCamera) {
                            repository.insertEvent(PermissionEvent(
                                packageName = currentPackage,
                                permissionType = "CAMERA",
                                timestamp = now
                            ))
                        }
                        
                        if (hasMic) {
                            repository.insertEvent(PermissionEvent(
                                packageName = currentPackage,
                                permissionType = "MICROPHONE",
                                timestamp = now
                            ))
                        }
                    } catch (e: Exception) { }
                }
                delay(3000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Permission Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors app permissions in background"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        var isRunning = false
    }
}
