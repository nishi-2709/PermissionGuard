package com.permissionguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.permissionguard.ui.ViewModelFactory
import com.permissionguard.ui.appdetail.AppDetailScreen
import com.permissionguard.ui.appdetail.AppDetailViewModel
import com.permissionguard.ui.apps.AppsScreen
import com.permissionguard.ui.apps.AppsViewModel
import com.permissionguard.ui.dashboard.DashboardScreen
import com.permissionguard.ui.dashboard.DashboardViewModel
import com.permissionguard.ui.monitor.MonitorScreen
import com.permissionguard.ui.monitor.MonitorViewModel
import com.permissionguard.ui.navigation.Screen
import com.permissionguard.ui.theme.PermissionGuardTheme

import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as PermissionGuardApplication).container
        val viewModelFactory = ViewModelFactory(appContainer)
        
        setContent {
            PermissionGuardTheme {
                PermissionGuardApp(viewModelFactory)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionGuardApp(viewModelFactory: ViewModelFactory) {
    val navController = rememberNavController()
    
    val items = listOf(
        Screen.Dashboard,
        Screen.Apps,
        Screen.Monitor
    )
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { 
                val viewModel: DashboardViewModel = viewModel(factory = viewModelFactory)
                DashboardScreen(viewModel) 
            }
            composable(Screen.Apps.route) { 
                val viewModel: AppsViewModel = viewModel(factory = viewModelFactory)
                AppsScreen(viewModel, onAppClick = { packageName ->
                    navController.navigate(Screen.AppDetail.createRoute(packageName))
                }) 
            }
            composable(Screen.Monitor.route) { 
                val viewModel: MonitorViewModel = viewModel(factory = viewModelFactory)
                MonitorScreen(viewModel) 
            }
            composable(
                route = Screen.AppDetail.route,
                arguments = listOf(navArgument("packageName") { type = NavType.StringType })
            ) { backStackEntry ->
                val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
                val viewModel: AppDetailViewModel = viewModel(factory = viewModelFactory)
                AppDetailScreen(packageName, viewModel, onBackClick = { navController.navigateUp() })
            }
        }
    }
}
