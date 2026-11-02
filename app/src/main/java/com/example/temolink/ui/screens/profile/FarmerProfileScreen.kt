package com.example.temolink.ui.screens.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.temolink.data.model.Produce
import com.example.temolink.data.model.User
import com.example.temolink.data.repository.MarketplaceRepository
import com.example.temolink.ui.components.AppImage
import com.example.temolink.ui.screens.marketplace.ProduceCard
import com.example.temolink.ui.theme.*
import com.example.temolink.ui.viewmodel.AuthViewModel
import com.example.temolink.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerProfileScreen(
    farmerId: String,
    onNavigateToProduce: (String) -> Unit,
    onNavigateToChat: (String, String) -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var farmer by remember { mutableStateOf<User?>(null) }
    var farmerProduce by remember { mutableStateOf<List<Produce>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val repository = MarketplaceRepository()
    val context = LocalContext.current

    LaunchedEffect(farmerId) {
        scope.launch {
            isLoading = true
            farmer = repository.getFarmerPublicProfile(farmerId)
            farmerProduce = repository.getProduceByFarmer(farmerId)
            isLoading = false
        }
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("Farmer Profile", color = PrimaryBlue, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (farmer == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Farmer not found", color = SecondaryText)
            }
        } else {
            val farmerData = farmer!!
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    FarmerHeader(
                        farmer = farmerData,
                        isMe = currentUser?.uid == farmerData.uid,
                        onMessageClick = {
                            if (currentUser == null) {
                                Toast.makeText(context, "Please login to send messages", Toast.LENGTH_SHORT).show()
                                return@FarmerHeader
                            }
                            currentUser?.let { buyer ->
                                scope.launch {
                                    val threadId = chatViewModel.getOrCreateThread(
                                        buyerId = buyer.uid,
                                        buyerName = buyer.fullName,
                                        farmerId = farmerData.uid,
                                        farmerName = farmerData.fullName
                                    )
                                    onNavigateToChat(threadId, farmerData.uid)
                                }
                            }
                        }
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "Active Listings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                if (farmerProduce.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No active listings from this farmer.", color = MutedText, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    items(farmerProduce) { produce ->
                        ProduceCard(produce = produce) {
                            onNavigateToProduce(produce.produceId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerHeader(
    farmer: User,
    isMe: Boolean,
    onMessageClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PanelBlue),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
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
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        tint = MutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = farmer.fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            if (farmer.farmName.isNotEmpty()) {
                Text(
                    text = farmer.farmName,
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = farmer.location,
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Details Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Contact Information",
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                
                ContactActionRow(
                    icon = Icons.Default.Phone, 
                    text = farmer.phoneNumber.ifEmpty { "No phone number" },
                    onClick = {
                        if (farmer.phoneNumber.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${farmer.phoneNumber}"))
                            context.startActivity(intent)
                        }
                    }
                )
                
                ContactActionRow(
                    icon = Icons.Default.Email, 
                    text = farmer.email,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${farmer.email}"))
                        context.startActivity(intent)
                    }
                )
            }

            if (farmer.produceTypes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Specializes in",
                    color = PrimaryText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 3
                ) {
                    farmer.produceTypes.forEach { type ->
                        Surface(
                            modifier = Modifier.padding(4.dp),
                            color = PrimaryGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = type,
                                color = PrimaryGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            if (!isMe) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onMessageClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Message Farmer", fontWeight = FontWeight.Bold)
                }
            }

            if (farmer.about.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "About the Farm",
                    color = PrimaryText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = farmer.about,
                    color = SecondaryText,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ContactActionRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = PrimaryText,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        if (text != "No phone number") {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Contact",
                    tint = PrimaryBlue.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        maxItemsInEachRow = maxItemsInEachRow
    ) {
        content()
    }
}
