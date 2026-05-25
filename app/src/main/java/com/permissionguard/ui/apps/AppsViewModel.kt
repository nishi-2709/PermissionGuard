package com.permissionguard.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.permissionguard.domain.model.AppInfo
import com.permissionguard.domain.model.RiskLevel
import com.permissionguard.domain.scanner.AppScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AppsUiState {
    object Loading : AppsUiState()
    data class Success(val apps: List<AppInfo>) : AppsUiState()
}

enum class AppFilter { ALL, HIGH, MEDIUM, LOW }

class AppsViewModel(private val appScanner: AppScanner) : ViewModel() {
    private val _uiState = MutableStateFlow<AppsUiState>(AppsUiState.Loading)
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    private var allApps: List<AppInfo> = emptyList()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentFilter = MutableStateFlow(AppFilter.ALL)
    val currentFilter: StateFlow<AppFilter> = _currentFilter.asStateFlow()

    init {
        scanApps()
    }

    fun refresh() {
        appScanner.clearCache()
        scanApps()
    }

    fun scanApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = AppsUiState.Loading
            allApps = appScanner.getInstalledApps()
            applyFilters()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun onFilterChanged(filter: AppFilter) {
        _currentFilter.value = filter
        applyFilters()
    }

    private fun applyFilters() {
        val query = _searchQuery.value.trim().lowercase()
        val filter = _currentFilter.value

        var filteredList = allApps

        if (query.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.appName.lowercase().contains(query) || it.packageName.lowercase().contains(query)
            }
        }

        filteredList = when (filter) {
            AppFilter.ALL -> filteredList
            AppFilter.HIGH -> filteredList.filter { it.riskLevel == RiskLevel.HIGH }
            AppFilter.MEDIUM -> filteredList.filter { it.riskLevel == RiskLevel.MEDIUM }
            AppFilter.LOW -> filteredList.filter { it.riskLevel == RiskLevel.LOW }
        }

        _uiState.value = AppsUiState.Success(filteredList)
    }
}
