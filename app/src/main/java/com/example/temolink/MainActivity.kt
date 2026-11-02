package com.example.temolink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.temolink.navigation.Screen
import com.example.temolink.ui.screens.auth.ForgotPasswordScreen
import com.example.temolink.ui.screens.auth.LoginScreen
import com.example.temolink.ui.screens.auth.RegisterScreen
import com.example.temolink.ui.screens.chat.ChatScreen
import com.example.temolink.ui.screens.chat.MessagesListScreen
import com.example.temolink.ui.screens.dashboard.AddProduceScreen
import com.example.temolink.ui.screens.dashboard.BuyerDashboardScreen
import com.example.temolink.ui.screens.dashboard.EditProduceScreen
import com.example.temolink.ui.screens.dashboard.FarmerDashboardScreen
import com.example.temolink.ui.screens.dashboard.ManageListingsScreen
import com.example.temolink.ui.screens.landing.LandingScreen
import com.example.temolink.ui.screens.marketplace.MarketplaceScreen
import com.example.temolink.ui.screens.marketplace.ProduceDetailsScreen
import com.example.temolink.ui.screens.notifications.NotificationsScreen
import com.example.temolink.ui.screens.profile.FarmerProfileScreen
import com.example.temolink.ui.screens.profile.ProfileScreen
import com.example.temolink.ui.theme.*
import com.example.temolink.ui.viewmodel.AuthViewModel
import com.example.temolink.ui.viewmodel.ChatViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemoLinkTheme {
                TemoLinkApp()
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun TemoLinkApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val user by authViewModel.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val unreadMessages by if (user != null) {
        chatViewModel.getUnreadCount(user!!.uid).collectAsState(initial = 0)
    } else {
        remember { mutableStateOf(0) }
    }

    LaunchedEffect(user) {
        user?.let {
            chatViewModel.loadThreads(it.uid, it.role.name)
        }
    }

    val bottomNavItems = remember(user) {
        val dashboardRoute = if (user?.role?.name == "FARMER") Screen.FarmerDashboard.route else Screen.BuyerDashboard.route
        listOf(
            BottomNavItem("Home", Icons.Default.Home, dashboardRoute),
            BottomNavItem("Market", Icons.Default.ShoppingCart, Screen.Marketplace.route),
            BottomNavItem("Messages", Icons.Default.Email, Screen.Messages.route),
            BottomNavItem("Profile", Icons.Default.Person, Screen.Profile.route)
        )
    }

    val showBottomBar = currentDestination?.route in listOf(
        Screen.FarmerDashboard.route,
        Screen.BuyerDashboard.route,
        Screen.Marketplace.route,
        Screen.Messages.route,
        Screen.Profile.route
    )

    Scaffold(
        containerColor = DarkNavy,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Column {
                    HorizontalDivider(
                        color = PrimaryBlue.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                    NavigationBar(
                        containerColor = SecondaryNavy,
                        tonalElevation = 0.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                icon = { 
                                    BadgedBox(
                                        badge = {
                                            if (item.route == Screen.Messages.route && unreadMessages > 0) {
                                                Badge { Text(unreadMessages.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = item.icon, 
                                            contentDescription = item.label,
                                            tint = if (selected) Color.White else MutedText
                                        ) 
                                    }
                                },
                                label = { 
                                    Text(
                                        text = item.label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ) 
                                },
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    unselectedIconColor = MutedText,
                                    selectedTextColor = PrimaryBlue,
                                    unselectedTextColor = MutedText,
                                    indicatorColor = PrimaryBlue.copy(alpha = 0.25f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Landing.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Landing.route) {
                LandingScreen(
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToRegister = { role -> 
                        navController.navigate(Screen.Register.createRoute(role ?: "")) 
                    },
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) }
                )
            }
            
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Screen.Register.createRoute("")) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = { role ->
                        val dest = if (role == "FARMER") Screen.FarmerDashboard.route else Screen.BuyerDashboard.route
                        navController.navigate(dest) {
                            popUpTo(Screen.Landing.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.Register.route,
                arguments = listOf(navArgument("role") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val role = backStackEntry.arguments?.getString("role")
                RegisterScreen(
                    initialRole = role,
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onRegisterSuccess = { userRole ->
                        val dest = if (userRole == "FARMER") Screen.FarmerDashboard.route else Screen.BuyerDashboard.route
                        navController.navigate(dest) {
                            popUpTo(Screen.Landing.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Marketplace.route) {
                MarketplaceScreen(
                    onNavigateToProduce = { id -> navController.navigate(Screen.ProduceDetails.createRoute(id)) }
                )
            }

            composable(
                route = Screen.ProduceDetails.route,
                arguments = listOf(navArgument("produceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val produceId = backStackEntry.arguments?.getString("produceId") ?: ""
                ProduceDetailsScreen(
                    produceId = produceId,
                    onNavigateToFarmer = { farmerId -> navController.navigate(Screen.FarmerProfile.createRoute(farmerId)) },
                    onNavigateToChat = { tId, oId -> navController.navigate(Screen.Chat.createRoute(tId, oId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.FarmerProfile.route,
                arguments = listOf(navArgument("farmerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val farmerId = backStackEntry.arguments?.getString("farmerId") ?: ""
                FarmerProfileScreen(
                    farmerId = farmerId,
                    onNavigateToProduce = { id -> navController.navigate(Screen.ProduceDetails.createRoute(id)) },
                    onNavigateToChat = { tId, oId -> navController.navigate(Screen.Chat.createRoute(tId, oId)) },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.FarmerDashboard.route) {
                FarmerDashboardScreen(
                    onNavigateToAddProduce = { navController.navigate(Screen.AddProduce.route) },
                    onNavigateToManageListings = { navController.navigate(Screen.ManageListings.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) }
                )
            }
            
            composable(Screen.BuyerDashboard.route) {
                BuyerDashboardScreen(
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
                )
            }

            composable(Screen.AddProduce.route) {
                AddProduceScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }

            composable(Screen.ManageListings.route) {
                ManageListingsScreen(
                    onBack = { navController.popBackStack() },
                    onEditProduce = { id -> navController.navigate(Screen.EditProduce.createRoute(id)) }
                )
            }

            composable(
                route = Screen.EditProduce.route,
                arguments = listOf(navArgument("produceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val produceId = backStackEntry.arguments?.getString("produceId") ?: ""
                EditProduceScreen(
                    produceId = produceId,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Landing.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.Messages.route) {
                MessagesListScreen(
                    onNavigateToChat = { tId, oId -> navController.navigate(Screen.Chat.createRoute(tId, oId)) }
                )
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("threadId") { type = NavType.StringType },
                    navArgument("otherUserId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val threadId = backStackEntry.arguments?.getString("threadId") ?: ""
                val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
                ChatScreen(
                    threadId = threadId,
                    otherUserId = otherUserId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
