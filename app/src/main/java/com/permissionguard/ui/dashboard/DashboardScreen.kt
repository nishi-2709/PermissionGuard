package com.permissionguard.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
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

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "PermissionGuard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Scan",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { viewModel.refresh() }
                )
            }
        }

        // Circular Score
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var animationPlayed by remember { mutableStateOf(false) }
                val targetProgress = uiState.securityScore / 100f
                val animatedProgress by animateFloatAsState(
                    targetValue = if (animationPlayed) targetProgress else 0f,
                    animationSpec = tween(durationMillis = 1500),
                    label = "ScoreAnimation"
                )

                LaunchedEffect(key1 = true) {
                    animationPlayed = true
                }

                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 12.dp,
                        color = when {
                            uiState.securityScore >= 80 -> MaterialTheme.colorScheme.primary
                            uiState.securityScore >= 50 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.securityScore}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "SECURITY SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (uiState.securityScore >= 80) "Your privacy looks good" else "Action required on privacy",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Last scan: Just now",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Risk Distribution Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RiskStatDotItem("Safe", uiState.lowRiskApps, Color(0xFF00E676))
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    RiskStatDotItem("Medium", uiState.mediumRiskApps, Color(0xFFFFB300))
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    RiskStatDotItem("High", uiState.highRiskApps, Color(0xFFFF3366))
                }
            }
        }

        // 2x2 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    GridCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.List,
                        iconTint = MaterialTheme.colorScheme.secondary, // Purple
                        value = uiState.totalApps.toString(),
                        label = "Total Apps Scanned"
                    )
                    GridCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Warning,
                        iconTint = MaterialTheme.colorScheme.error, // Red
                        value = uiState.totalDangerousPermissions.toString(),
                        label = "Dangerous Permissions"
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    GridCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Info,
                        iconTint = MaterialTheme.colorScheme.primary, // Cyan
                        value = uiState.totalLoggedEvents.toString(),
                        label = "Active Sensor Events"
                    )
                    GridCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AccountCircle,
                        iconTint = MaterialTheme.colorScheme.secondary, // Purple
                        value = (uiState.totalLoggedEvents * 14).toString(), // Placeholder multiplier
                        label = "Background Access"
                    )
                }
            }
        }

        // Recent Activity
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recent Activity", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text("See all", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.recentEvents.isEmpty()) {
                        Text("No recent activity.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                        uiState.recentEvents.forEachIndexed { index, event ->
                            val timeString = format.format(java.util.Date(event.timestamp))
                            val isMic = event.permissionType.contains("AUDIO", ignoreCase = true) || event.permissionType.contains("MICROPHONE", ignoreCase = true)
                            val type = if (isMic) "Microphone access" else "Camera access"
                            
                            val context = LocalContext.current
                            var appName by remember { mutableStateOf(event.packageName) }
                            
                            LaunchedEffect(event.packageName) {
                                try {
                                    val pm = context.packageManager
                                    val appInfo = pm.getApplicationInfo(event.packageName, 0)
                                    appName = pm.getApplicationLabel(appInfo).toString()
                                } catch (e: Exception) {}
                            }
                            
                            Text(appName, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Text("$type • $timeString", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            
                            if (index < uiState.recentEvents.size - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }

        // Privacy Tip
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF063A36)), // Teal background
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Privacy Tip", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            uiState.privacyTip,
                            color = Color(0xCCFFFFFF),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RiskStatDotItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = count.toString(), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun GridCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, value: String, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
