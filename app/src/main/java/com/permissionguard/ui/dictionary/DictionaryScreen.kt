package com.permissionguard.ui.dictionary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.permissionguard.domain.model.PermissionEntry
import com.permissionguard.domain.model.RiskLevel

@Composable
fun DictionaryScreen(viewModel: DictionaryViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val entries by viewModel.filteredEntries.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search permissions...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries) { entry ->
                PermissionCard(entry)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionCard(entry: PermissionEntry) {
    val riskColor = when (entry.riskLevel) {
        RiskLevel.HIGH -> MaterialTheme.colorScheme.error
        RiskLevel.MEDIUM -> Color(0xFFFFB300)
        RiskLevel.LOW -> Color(0xFF4CAF50)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = entry.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Badge(containerColor = riskColor) {
                    Text(text = entry.riskLevel.name, modifier = Modifier.padding(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = entry.id, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = entry.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
