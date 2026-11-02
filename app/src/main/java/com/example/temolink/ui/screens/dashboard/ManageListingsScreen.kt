package com.example.temolink.ui.screens.dashboard

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.temolink.data.model.Produce
import com.example.temolink.data.repository.ImageRepository
import com.example.temolink.data.repository.MarketplaceRepository
import com.example.temolink.ui.screens.marketplace.ProduceCard
import com.example.temolink.ui.theme.*
import com.example.temolink.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageListingsScreen(
    onBack: () -> Unit,
    onEditProduce: (String) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val user by authViewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    var listings by remember { mutableStateOf<List<Produce>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var produceToDelete by remember { mutableStateOf<Produce?>(null) }
    var isSyncing by remember { mutableStateOf(false) }

    val repository = remember { MarketplaceRepository() }
    val imageRepository = remember { ImageRepository(context) }

    fun loadListings() {
        user?.let {
            isLoading = true
            scope.launch {
                listings = repository.getProduceByFarmer(it.uid)
                isLoading = false
            }
        }
    }

    LaunchedEffect(user) {
        loadListings()
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("Manage Listings", color = PrimaryBlue, fontWeight = FontWeight.Bold) },
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
        } else if (listings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't listed any produce yet.", color = SecondaryText)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(listings) { produce ->
                    val isLocalImage = produce.imageUrl.startsWith("content://") || produce.imageUrl.startsWith("file://")
                    
                    Box {
                        ProduceCard(produce = produce) {
                            onEditProduce(produce.produceId)
                        }
                        
                        // Top End: Delete Action
                        IconButton(
                            onClick = { produceToDelete = produce },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                        }

                        // Bottom End: Migration/Sync Action for local images
                        if (isLocalImage) {
                            IconButton(
                                onClick = {
                                    if (!isSyncing) {
                                        isSyncing = true
                                        scope.launch {
                                            val result = imageRepository.uploadImage(Uri.parse(produce.imageUrl), "produce")
                                            result.onSuccess { newUrl ->
                                                repository.updateProduceListing(produce.copy(imageUrl = newUrl))
                                                loadListings()
                                            }
                                            isSyncing = false
                                        }
                                    }
                                },
                                modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                                colors = IconButtonDefaults.iconButtonColors(containerColor = PrimaryBlue.copy(alpha = 0.8f))
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = "Sync to Cloud", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (produceToDelete != null) {
        AlertDialog(
            onDismissRequest = { produceToDelete = null },
            title = { Text("Delete Listing?") },
            text = { Text("Are you sure you want to delete '${produceToDelete?.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteProduceListing(produceToDelete!!.produceId)
                            produceToDelete = null
                            loadListings()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { produceToDelete = null }) {
                    Text("Cancel", color = PrimaryText)
                }
            },
            containerColor = PanelBlue,
            titleContentColor = PrimaryText,
            textContentColor = SecondaryText
        )
    }
}
