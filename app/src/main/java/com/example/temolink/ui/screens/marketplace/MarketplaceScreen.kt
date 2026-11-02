package com.example.temolink.ui.screens.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.temolink.data.model.Produce
import com.example.temolink.ui.components.AppImage
import com.example.temolink.ui.theme.*
import com.example.temolink.ui.viewmodel.MarketplaceState
import com.example.temolink.ui.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onNavigateToProduce: (String) -> Unit,
    viewModel: MarketplaceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedLocation by remember { mutableStateOf("All") }
    var showFilterDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Vegetables", "Fruit", "Grains", "Poultry", "Livestock", "Dairy")
    val locations = listOf("All", "Maseru", "Leribe", "Berea", "Mafeteng", "Mohale's Hoek", "Mokhotlong", "Qacha's Nek", "Quthing", "Thaba-Tseka", "Butha-Buthe")

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("Marketplace", color = PrimaryBlue, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search produce...", color = MutedText) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MutedText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = PanelBlue,
                    focusedContainerColor = SecondaryNavy,
                    unfocusedContainerColor = SecondaryNavy,
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Current Filters Display
            if (selectedCategory != "All" || selectedLocation != "All") {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedCategory != "All") {
                        FilterChip(selectedCategory) { selectedCategory = "All" }
                    }
                    if (selectedLocation != "All") {
                        FilterChip(selectedLocation) { selectedLocation = "All" }
                    }
                }
            }

            when (val state = uiState) {
                is MarketplaceState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is MarketplaceState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
                is MarketplaceState.Success -> {
                    val filteredList = state.produceList.filter {
                        (it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)) &&
                        (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)) &&
                        (selectedLocation == "All" || it.location.equals(selectedLocation, ignoreCase = true))
                    }

                    if (filteredList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No produce found matching your criteria.", color = SecondaryText)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredList) { produce ->
                                ProduceCard(produce = produce) {
                                    onNavigateToProduce(produce.produceId)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Marketplace", color = PrimaryText) },
            text = {
                Column {
                    Text("Category", color = MutedText, fontSize = 12.sp)
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                        containerColor = Color.Transparent,
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        categories.forEach { category ->
                            Tab(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                text = { Text(category) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Location", color = MutedText, fontSize = 12.sp)
                    var locationExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { locationExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedLocation)
                        }
                        DropdownMenu(
                            expanded = locationExpanded,
                            onDismissRequest = { locationExpanded = false },
                            modifier = Modifier.background(PanelBlue)
                        ) {
                            locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc, color = PrimaryText) },
                                    onClick = {
                                        selectedLocation = loc
                                        locationExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFilterDialog = false }) {
                    Text("Apply Filters")
                }
            },
            containerColor = SecondaryNavy
        )
    }
}

@Composable
fun FilterChip(label: String, onRemove: () -> Unit) {
    Surface(
        color = PrimaryBlue.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f)),
        modifier = Modifier.clickable { onRemove() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = PrimaryBlue, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "×", color = PrimaryBlue, fontSize = 16.sp)
        }
    }
}

@Composable
fun ProduceCard(produce: Produce, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PanelBlue),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
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
                
                if (!produce.availability) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Sold Out", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = produce.name,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "M${produce.price} / ${produce.unit}",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = produce.farmerName,
                    color = MutedText,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Text(
                    text = produce.location,
                    color = MutedText,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}
