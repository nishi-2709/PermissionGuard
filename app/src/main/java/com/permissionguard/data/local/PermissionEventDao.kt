package com.permissionguard.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.permissionguard.domain.model.PermissionEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface PermissionEventDao {
    @Insert
    suspend fun insert(event: PermissionEvent)

    @Query("SELECT * FROM permission_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<PermissionEvent>>

    @Query("DELETE FROM permission_events")
    suspend fun clearAll()
}
