package com.example.temolink.ui.screens.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.temolink.ui.components.AppImage
import com.example.temolink.ui.theme.*
import com.example.temolink.ui.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduceDetailsScreen(
    produceId: String,
    onNavigateToFarmer: (String) -> Unit,
    onNavigateToChat: (String, String) -> Unit,
    onBack: () -> Unit,
    marketplaceViewModel: MarketplaceViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val detailsState by marketplaceViewModel.detailsState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    var isStartingChat by remember { mutableStateOf(false) }

    LaunchedEffect(produceId) {
        marketplaceViewModel.loadProduceDetails(produceId)
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("Details", color = PrimaryBlue) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { paddingValues ->
        when (val state = detailsState) {
            is ProduceDetailsState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            is ProduceDetailsState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = Color.Red)
                }
            }
            is ProduceDetailsState.Success -> {
                val produce = state.produce
                val farmer = state.farmer

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(SecondaryNavy)
                    ) {
                        if (produce.imageUrl.isNotEmpty()) {
                            AppImage(
                                model = produce.imageUrl,
                                contentDescription = produce.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = produce.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "M${produce.price} / ${produce.unit}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (produce.availability) {
                                Surface(
                                    color = PrimaryGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Available",
                                        color = PrimaryGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimaryText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = produce.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SecondaryText
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PanelBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Farmer Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(SecondaryNavy)
                                    ) {
                                        if (farmer.profileImageUrl.isNotEmpty()) {
                                            AppImage(
                                                model = farmer.profileImageUrl,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = farmer.fullName, color = PrimaryText, fontWeight = FontWeight.Bold)
                                        Text(text = farmer.location, color = MutedText, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { onNavigateToFarmer(farmer.uid) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryNavy),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("View Farmer Profile", color = PrimaryBlue)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        if (currentUser != null && currentUser?.uid != produce.farmerId) {
                            Button(
                                onClick = {
                                    if (isStartingChat) return@Button
                                    isStartingChat = true
                                    scope.launch {
                                        try {
                                            val threadId = chatViewModel.getOrCreateThread(
                                                buyerId = currentUser!!.uid,
                                                buyerName = currentUser!!.fullName,
                                                farmerId = farmer.uid,
                                                farmerName = farmer.fullName
                                            )
                                            onNavigateToChat(threadId, farmer.uid)
                                        } catch (e: Exception) {
                                            isStartingChat = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isStartingChat
                            ) {
                                if (isStartingChat) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Message Farmer", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        } else if (currentUser == null) {
                            Text(
                                "Please login to contact the farmer.",
                                color = MutedText,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
