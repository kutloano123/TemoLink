package com.example.temolink.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.temolink.data.repository.MarketplaceRepository
import com.example.temolink.ui.components.ActionCard
import com.example.temolink.ui.components.StatCard
import com.example.temolink.ui.theme.*
import com.example.temolink.ui.viewmodel.AuthViewModel
import com.example.temolink.ui.viewmodel.ChatViewModel
import com.example.temolink.ui.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDashboardScreen(
    onNavigateToAddProduce: () -> Unit,
    onNavigateToManageListings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val user by authViewModel.currentUser.collectAsState()
    val unreadNotifications by notificationViewModel.unreadCount.collectAsState()
    val unreadMessages by if (user != null) {
        chatViewModel.getUnreadCount(user!!.uid).collectAsState(initial = 0)
    } else {
        remember { mutableStateOf(0) }
    }

    var activeListingsCount by remember { mutableIntStateOf(0) }

    val marketplaceRepository = MarketplaceRepository()
    val scope = rememberCoroutineScope()

    LaunchedEffect(user) {
        user?.let {
            scope.launch {
                activeListingsCount = marketplaceRepository.getProduceByFarmer(it.uid).size
            }
            chatViewModel.loadThreads(it.uid, it.role.name)
            notificationViewModel.loadNotifications(it.uid)
        }
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("Farmer Dashboard", color = PrimaryBlue, fontWeight = FontWeight.Bold) },
                actions = {
                    BadgedBox(
                        badge = {
                            if (unreadNotifications > 0) {
                                Badge { Text(unreadNotifications.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = PrimaryText)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Welcome, ${user?.fullName ?: "Farmer"}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Here's what's happening today",
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Active Listings",
                        value = activeListingsCount.toString(),
                        icon = Icons.AutoMirrored.Filled.List,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Unread Messages",
                        value = unreadMessages.toString(),
                        icon = Icons.Default.Email,
                        color = LightBlue
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ActionCard("Browse Marketplace", "See what other farmers are selling", Icons.Default.Search, PrimaryBlue, onNavigateToMarketplace)
                Spacer(modifier = Modifier.height(12.dp))
                ActionCard("Add New Produce", "Create a new listing for buyers", Icons.Default.Add, PrimaryGreen, onNavigateToAddProduce)
                Spacer(modifier = Modifier.height(12.dp))
                ActionCard("Manage Listings", "Edit or remove your active produce", Icons.Default.Edit, PrimaryBlue, onNavigateToManageListings)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
