package com.example.temolink.ui.screens.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temolink.data.model.Produce
import com.example.temolink.data.repository.ImageRepository
import com.example.temolink.data.repository.MarketplaceRepository
import com.example.temolink.ui.components.AppImage
import com.example.temolink.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProduceScreen(
    produceId: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: MarketplaceRepository = MarketplaceRepository()
) {
    val context = LocalContext.current
    var produce by remember { mutableStateOf<Produce?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var quantity by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf(true) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnitDropdown by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val imageRepository = remember { ImageRepository(context) }
    val units = listOf("kg", "g", "packet", "bag", "box", "crate", "liter", "unit")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(produceId) {
        val p = viewModel.getProduceById(produceId)

        if (p != null) {
            produce = p
            name = p.name
            description = p.description
            category = p.category
            price = p.price.toString()
            unit = p.unit
            quantity = p.quantity.toString()
            location = p.location
            imageUrl = p.imageUrl
            availability = p.availability
        }

        isLoading = false
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Produce",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryText
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy
                )
            )
        }
    ) { paddingValues ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (produce == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Produce not found", color = SecondaryText)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // IMAGE
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).background(PanelBlue).border(1.dp, SecondaryNavy, RoundedCornerShape(12.dp)).clickable {
                            imagePickerLauncher.launch("image/*")
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AppImage(model = selectedImageUri, contentDescription = "New Image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else if (imageUrl.isNotEmpty()) {
                            AppImage(model = imageUrl, contentDescription = "Current Image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "📷", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Change Produce Image", color = MutedText)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // NAME
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Produce Name") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = PanelBlue, focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText), shape = RoundedCornerShape(8.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // DESCRIPTION
                item {
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(120.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = PanelBlue, focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText), shape = RoundedCornerShape(8.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // AVAILABILITY
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = availability, onCheckedChange = { availability = it }, colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen))
                        Text("Available for sale", color = PrimaryText)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // PRICE AND UNIT
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (M)") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = PanelBlue, focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText), shape = RoundedCornerShape(8.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("Unit") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { showUnitDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = PrimaryBlue)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = PanelBlue,
                                    focusedTextColor = PrimaryText,
                                    unfocusedTextColor = PrimaryText
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            DropdownMenu(
                                expanded = showUnitDropdown,
                                onDismissRequest = { showUnitDropdown = false },
                                modifier = Modifier.background(PanelBlue)
                            ) {
                                units.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u, color = PrimaryText) },
                                        onClick = {
                                            unit = u
                                            showUnitDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // QUANTITY
                item {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = PanelBlue, focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText), shape = RoundedCornerShape(8.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // SAVE
                item {
                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Button(
                        onClick = {
                            isSaving = true
                            errorMessage = null
                            scope.launch {
                                var finalImageUrl = imageUrl
                                if (selectedImageUri != null) {
                                    val uploadResult = imageRepository.uploadImage(selectedImageUri!!, "produce")
                                    if (uploadResult.isSuccess) {
                                        finalImageUrl = uploadResult.getOrDefault(imageUrl)
                                    } else {
                                        isSaving = false
                                        errorMessage = uploadResult.exceptionOrNull()?.message ?: "Failed to upload image"
                                        return@launch
                                    }
                                }

                                val updatedProduce = produce!!.copy(
                                    name = name,
                                    description = description,
                                    category = category,
                                    price = price.toDoubleOrNull() ?: produce!!.price,
                                    unit = unit,
                                    quantity = quantity.toIntOrNull() ?: produce!!.quantity,
                                    location = location,
                                    imageUrl = finalImageUrl,
                                    availability = availability,
                                    updatedAt = System.currentTimeMillis()
                                )

                                viewModel.updateProduceListing(updatedProduce)
                                    .onSuccess { isSaving = false; onSuccess() }
                                    .onFailure { isSaving = false; errorMessage = it.message ?: "Failed to update" }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // DELETE DIALOG
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Listing") },
            text = { Text("Are you sure you want to delete this produce listing? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.deleteProduceListing(produceId)
                        showDeleteDialog = false
                        onSuccess()
                    }
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = PrimaryText)
                }
            },
            containerColor = PanelBlue,
            titleContentColor = PrimaryText,
            textContentColor = SecondaryText
        )
    }
}
