package com.permissionguard.ui.monitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.permissionguard.domain.model.PermissionEvent
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonitorScreen(viewModel: MonitorViewModel) {
    val context = LocalContext.current
    val events by viewModel.events.collectAsState()
    val filteredEvents by viewModel.filteredEvents.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()

    var showHelpDialog by remember { mutableStateOf(false) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
            if (isGranted) {
                viewModel.toggleService(context)
            }
        }
    )

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
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Monitor",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Help",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { showHelpDialog = true }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // System Service Toggle Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10183A)), // Deep blue specific to this card
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(12.dp)) // Shield placeholder
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("SYSTEM SERVICE", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isServiceRunning) "Monitoring Active" else "Monitoring Paused",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Real-time sensor surveillance is ${if (isServiceRunning) "on" else "off"}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Switch(
                            checked = isServiceRunning,
                            onCheckedChange = {
                                if (!isServiceRunning && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.toggleService(context)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.secondary)
                        )
                    }
                }
            }

            // Live Status
            item {
                Column {
                    Text("LIVE STATUS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val now = System.currentTimeMillis()
                    val twoMins = 2 * 60 * 1000L
                    
                    val recentMicEvent = events.firstOrNull { it.permissionType.contains("AUDIO", ignoreCase = true) || it.permissionType.contains("MICROPHONE", ignoreCase = true) }
                    val isMicActive = isServiceRunning && recentMicEvent != null && (now - recentMicEvent.timestamp) < twoMins
                    val micStatusStr = if (isMicActive) "In Use" else "Idle"
                    val micTimeStr = recentMicEvent?.let { 
                        val diffMins = (now - it.timestamp) / 60000 
                        if (diffMins < 1) "JUST NOW" else "$diffMins MINS AGO"
                    } ?: "NO RECENT DATA"
                    
                    val recentCamEvent = events.firstOrNull { it.permissionType.contains("CAMERA", ignoreCase = true) }
                    val isCamActive = isServiceRunning && recentCamEvent != null && (now - recentCamEvent.timestamp) < twoMins
                    val camStatusStr = if (isCamActive) "In Use" else "Idle"
                    val camTimeStr = recentCamEvent?.let { 
                        val diffMins = (now - it.timestamp) / 60000 
                        if (diffMins < 1) "JUST NOW" else "$diffMins MINS AGO"
                    } ?: "NO RECENT DATA"

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LiveStatusCard(modifier = Modifier.weight(1f), icon = Icons.Default.Call, title = "Microphone", status = micStatusStr, time = micTimeStr, isActive = isMicActive)
                        LiveStatusCard(modifier = Modifier.weight(1f), icon = Icons.Default.AccountCircle, title = "Camera", status = camStatusStr, time = camTimeStr, isActive = isCamActive)
                    }
                }
            }

            // Today's Activity Stats
            item {
                Column {
                    Text("TODAY'S ACTIVITY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val micCount = events.count { it.permissionType.contains("AUDIO", ignoreCase = true) || it.permissionType.contains("MICROPHONE", ignoreCase = true) }
                    val camCount = events.count { it.permissionType.contains("CAMERA", ignoreCase = true) }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ActivityStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Call, count = micCount.toString(), label = "MIC ACCESSES")
                        ActivityStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.AccountCircle, count = camCount.toString(), label = "CAMERA ACCESSES")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ActivityStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Warning, count = events.size.toString(), label = "BACKGROUND EVENTS")
                        ActivityStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Warning, count = "0", label = "HIGH-RISK EVENTS")
                    }
                }
            }

            // Access Timeline
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ACCESS TIMELINE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChipUI(
                            label = "All",
                            isSelected = selectedFilter == MonitorViewModel.MonitorFilter.ALL,
                            onClick = { viewModel.setFilter(MonitorViewModel.MonitorFilter.ALL) }
                        )
                        FilterChipUI(
                            label = "Mic",
                            isSelected = selectedFilter == MonitorViewModel.MonitorFilter.MIC,
                            onClick = { viewModel.setFilter(MonitorViewModel.MonitorFilter.MIC) }
                        )
                        FilterChipUI(
                            label = "Camera",
                            isSelected = selectedFilter == MonitorViewModel.MonitorFilter.CAMERA,
                            onClick = { viewModel.setFilter(MonitorViewModel.MonitorFilter.CAMERA) }
                        )
                    }
                }
            }

            // Timeline Events
            items(filteredEvents) { event ->
                TimelineEventItem(event)
            }
            
            if (filteredEvents.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No events matching this filter.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { viewModel.clearLogs() },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("Clear Timeline Logs", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Permission Monitoring Guide", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "PermissionGuard uses background active polling to trace which apps use sensitive sensors.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Text(
                        "⚙️ To track background apps properly:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "1. Ensure 'Usage Access' permission is granted in System settings.\n" +
                        "2. Keep the system service 'Active' (Toggle on the switch in this page).\n" +
                        "3. On Realme/ColorOS, enable 'Disable permission monitoring' in Android Developer Options to avoid OS overrides.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) {
                    Text("Got it")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun FilterChipUI(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant) else null
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun LiveStatusCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, status: String, time: String, isActive: Boolean) {
    val statusColor = if (isActive) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(36.dp)) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
                }
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(status, style = MaterialTheme.typography.bodySmall, color = statusColor, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ActivityStatCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, count: String, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(count, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    label, 
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    letterSpacing = 0.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TimelineEventItem(event: PermissionEvent) {
    val isMic = event.permissionType.contains("AUDIO", ignoreCase = true) || event.permissionType.contains("MICROPHONE", ignoreCase = true)
    val dotColor = if (isMic) MaterialTheme.colorScheme.secondary else Color(0xFFFF3366)
    
    val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
    val timeString = format.format(java.util.Date(event.timestamp))
    
    val context = LocalContext.current
    var appName by remember { mutableStateOf(event.packageName) }
    
    LaunchedEffect(event.packageName) {
        try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(event.packageName, 0)
            appName = pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {}
    }

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Vertical line + Dot
        Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.TopCenter) {
            Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(MaterialTheme.colorScheme.surfaceVariant))
            Box(modifier = Modifier.padding(top = 16.dp).size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background).padding(2.dp)) {
                Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(dotColor))
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Event Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                    // App icon placeholder
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(appName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(timeString, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isMic) "Microphone Access" else "Camera Access",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
