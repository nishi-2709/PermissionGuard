package com.permissionguard.ui.monitor

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.permissionguard.data.repository.PermissionRepository
import com.permissionguard.domain.model.PermissionEvent
import com.permissionguard.service.PermissionMonitorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MonitorViewModel(private val repository: PermissionRepository) : ViewModel() {

    enum class MonitorFilter { ALL, MIC, CAMERA }

    private val _selectedFilter = MutableStateFlow(MonitorFilter.ALL)
    val selectedFilter: StateFlow<MonitorFilter> = _selectedFilter.asStateFlow()

    val events: StateFlow<List<PermissionEvent>> = repository.getAllEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredEvents: StateFlow<List<PermissionEvent>> = combine(
        repository.getAllEvents(),
        _selectedFilter
    ) { eventsList, filter ->
        when (filter) {
            MonitorFilter.ALL -> eventsList
            MonitorFilter.MIC -> eventsList.filter { it.permissionType.contains("AUDIO", ignoreCase = true) || it.permissionType.contains("MICROPHONE", ignoreCase = true) }
            MonitorFilter.CAMERA -> eventsList.filter { it.permissionType.contains("CAMERA", ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isServiceRunning = MutableStateFlow(PermissionMonitorService.isRunning)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    fun toggleService(context: Context) {
        val intent = Intent(context, PermissionMonitorService::class.java)
        if (_isServiceRunning.value) {
            context.stopService(intent)
            _isServiceRunning.value = false
        } else {
            context.startForegroundService(intent)
            _isServiceRunning.value = true
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun setFilter(filter: MonitorFilter) {
        _selectedFilter.value = filter
    }
}
