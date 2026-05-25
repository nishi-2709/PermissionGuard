package com.permissionguard.data.repository

import com.permissionguard.data.local.PermissionEventDao
import com.permissionguard.domain.model.PermissionEvent
import kotlinx.coroutines.flow.Flow

class PermissionRepository(private val dao: PermissionEventDao) {
    
    fun getAllEvents(): Flow<List<PermissionEvent>> {
        return dao.getAllEvents()
    }

    suspend fun insertEvent(event: PermissionEvent) {
        dao.insert(event)
    }

    suspend fun clearLogs() {
        dao.clearAll()
    }
}
