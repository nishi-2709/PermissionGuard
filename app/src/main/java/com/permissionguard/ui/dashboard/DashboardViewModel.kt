package com.permissionguard.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.permissionguard.data.repository.PermissionRepository
import com.permissionguard.domain.model.RiskLevel
import com.permissionguard.domain.scanner.AppScanner
import com.permissionguard.domain.model.PermissionEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalApps: Int = 0,
    val highRiskApps: Int = 0,
    val mediumRiskApps: Int = 0,
    val lowRiskApps: Int = 0,
    val securityScore: Int = 100,
    val totalLoggedEvents: Int = 0,
    val totalDangerousPermissions: Int = 0,
    val recentEvents: List<PermissionEvent> = emptyList(),
    val privacyTip: String = "",
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val appScanner: AppScanner,
    private val repository: PermissionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        observeEvents()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        appScanner.clearCache()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = appScanner.getInstalledApps()
            val high = apps.count { it.riskLevel == RiskLevel.HIGH }
            val medium = apps.count { it.riskLevel == RiskLevel.MEDIUM }
            val low = apps.count { it.riskLevel == RiskLevel.LOW }
            
            // Calculate a balanced, weighted security score starting at 100
            // High risk apps deduct 1.0 point, Medium risk apps deduct 0.25 point
            val totalDeduction = (high * 1.0f) + (medium * 0.25f)
            val finalScore = (100f - totalDeduction).toInt().coerceIn(0, 100)
            
            val totalDanger = apps.sumOf { it.dangerousPermissions.size }
            
            val tip = if (high > 0) "You have $high high-risk apps. Consider revoking unused permissions."
            else if (medium > 0) "Review permissions for your $medium medium-risk apps."
            else "Great job! All your apps are considered low risk."
            
            _uiState.value = _uiState.value.copy(
                totalApps = apps.size,
                highRiskApps = high,
                mediumRiskApps = medium,
                lowRiskApps = low,
                securityScore = finalScore,
                totalDangerousPermissions = totalDanger,
                privacyTip = tip,
                isLoading = false
            )
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            repository.getAllEvents().collect { events ->
                _uiState.value = _uiState.value.copy(
                    totalLoggedEvents = events.size,
                    recentEvents = events.take(2)
                )
            }
        }
    }
}
