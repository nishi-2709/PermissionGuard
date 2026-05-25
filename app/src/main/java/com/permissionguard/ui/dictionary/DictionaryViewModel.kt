package com.permissionguard.ui.dictionary

import androidx.lifecycle.ViewModel
import com.permissionguard.utils.PermissionDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DictionaryViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val allEntries = PermissionDatabase.allPermissions

    private val _filteredEntries = MutableStateFlow(allEntries)
    val filteredEntries: StateFlow<List<com.permissionguard.domain.model.PermissionEntry>> = _filteredEntries.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _filteredEntries.value = if (query.isBlank()) {
            allEntries
        } else {
            allEntries.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.id.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }
}
