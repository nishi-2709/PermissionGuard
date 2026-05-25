package com.permissionguard.ui.appdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.permissionguard.domain.model.PermissionEntry
import com.permissionguard.domain.model.RiskLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(packageName: String, viewModel: AppDetailViewModel, onBackClick: () -> Unit) {
    val appInfo by viewModel.appInfo.collectAsState()
    val permissionDetails by viewModel.permissionDetails.collectAsState()
    
    var showDictionary by remember { mutableStateOf(false) }

    LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName)
    }

    if (appInfo == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    appInfo?.let { app ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = app.appName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                IconButton(onClick = { viewModel.loadAppDetails(app.packageName) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rescan",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (Icon & Badges)
                item {
                    val riskColor = when (app.riskLevel) {
                        RiskLevel.HIGH -> Color(0xFFFF3366)
                        RiskLevel.MEDIUM -> Color(0xFFFFB300)
                        RiskLevel.LOW -> Color(0xFF00E676)
                    }
                    val riskBgColor = when (app.riskLevel) {
                        RiskLevel.HIGH -> Color(0x22FF3366)
                        RiskLevel.MEDIUM -> Color(0x22FFB300)
                        RiskLevel.LOW -> Color(0x2200E676)
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            app.icon?.let { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "App Icon",
                                    modifier = Modifier.size(96.dp).clip(CircleShape)
                                )
                            }
                            // Badge overlapping at the bottom
                            Surface(
                                color = riskBgColor,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, riskColor.copy(alpha = 0.5f)),
                                modifier = Modifier.align(Alignment.BottomCenter).offset(y = 12.dp)
                            ) {
                                Text(
                                    text = app.riskLevel.name,
                                    color = riskColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = app.appName, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = app.packageName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Action Buttons
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val openSettings = {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", app.packageName, null)
                            }
                            context.startActivity(intent)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = openSettings,
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("App Settings", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = { viewModel.loadAppDetails(app.packageName) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Rescan App", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Why this app is risky (Placeholder design integrated with real reason)
                if (app.riskLevel != RiskLevel.LOW) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x1AFF3366)), // Dark red tint
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FF3366)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF3366), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Why this app is risky", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Real data point
                                RiskBulletPoint(app.riskReason)
                                
                                if (app.isSideloaded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    RiskBulletPoint("Installed from an unofficial or third-party source.")
                                }
                            }
                        }
                    }
                }

                // Permission Audit Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Permission Audit", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { showDictionary = true }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Dictionary",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }

                // Permission Cards
                items(permissionDetails) { entry ->
                    PermissionCardItem(entry, onDictionaryClick = { showDictionary = true })
                }

                if (permissionDetails.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("This app does not request any dangerous permissions.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Bottom Buttons
                item {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { 
                                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = android.net.Uri.fromParts("package", app.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), // Purple
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Restrict Permissions", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Rescan this Application",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.clickable { viewModel.loadAppDetails(app.packageName) }.padding(8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDictionary) {
        ModalBottomSheet(
            onDismissRequest = { showDictionary = false },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(16.dp)) {
                Text("Permission Dictionary", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Learn what different Android permissions do and why they might be risky.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                var searchQuery by remember { mutableStateOf("") }
                val filteredPermissions = remember(searchQuery) {
                    com.permissionguard.utils.PermissionDatabase.allPermissions.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.id.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    placeholder = { Text("Search permissions...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredPermissions) { entry ->
                        PermissionCardItem(entry = entry, onDictionaryClick = null, isDictionaryMode = true)
                    }
                }
            }
        }
    }
}

@Composable
fun RiskBulletPoint(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.padding(top = 6.dp).size(6.dp).clip(CircleShape).background(Color(0xFFFF3366)))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = Color(0xDDFFFFFF), style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
    }
}

@Composable
fun PermissionCardItem(entry: PermissionEntry, onDictionaryClick: (() -> Unit)? = null, isDictionaryMode: Boolean = false) {
    var expanded by remember { mutableStateOf(isDictionaryMode) }
    
    val riskColor = when (entry.riskLevel) {
        RiskLevel.HIGH -> Color(0xFFFF3366)
        RiskLevel.MEDIUM -> Color(0xFFFFB300)
        RiskLevel.LOW -> Color(0xFF00E676)
    }
    val riskBgColor = when (entry.riskLevel) {
        RiskLevel.HIGH -> Color(0x22FF3366)
        RiskLevel.MEDIUM -> Color(0x22FFB300)
        RiskLevel.LOW -> Color(0x2200E676)
    }
    
    // Simple icon matching
    val icon = when {
        entry.name.contains("Camera", ignoreCase = true) -> Icons.Default.AccountBox // Placeholder for Camera
        entry.name.contains("Location", ignoreCase = true) -> Icons.Default.LocationOn
        entry.name.contains("Microphone", ignoreCase = true) || entry.name.contains("Record", ignoreCase = true) -> Icons.Default.Call // Placeholder for Mic
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = entry.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = riskBgColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = entry.riskLevel.name,
                                color = riskColor,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp, start = 40.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = entry.id,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = entry.description, style = MaterialTheme.typography.bodyMedium, color = Color.White, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!isDictionaryMode && onDictionaryClick != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onDictionaryClick() }.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BROWSE FULL DICTIONARY", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
