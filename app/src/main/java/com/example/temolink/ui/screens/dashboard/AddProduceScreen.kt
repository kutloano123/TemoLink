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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.temolink.data.model.Produce
import com.example.temolink.data.repository.ImageRepository
import com.example.temolink.data.repository.MarketplaceRepository
import com.example.temolink.ui.components.AppImage
import com.example.temolink.ui.theme.*
import com.example.temolink.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProduceScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val user by authViewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    val repository = remember { MarketplaceRepository() }
    val imageRepository = remember { ImageRepository(context) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var quantity by remember { mutableStateOf("") }
    var location by remember {
        mutableStateOf(user?.location ?: "Maseru")
    }

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var showLocationDropdown by remember {
        mutableStateOf(false)
    }

    var showUnitDropdown by remember {
        mutableStateOf(false)
    }

    val districts = listOf(
        "Maseru", "Leribe", "Berea", "Mafeteng", "Mohale's Hoek",
        "Mokhotlong", "Qacha's Nek", "Quthing", "Thaba-Tseka", "Butha-Buthe"
    )

    val units = listOf("kg", "g", "packet", "bag", "box", "crate", "liter", "unit")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Produce",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryText
                        )
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // IMAGE PICKER
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelBlue)
                        .border(
                            width = 1.dp,
                            color = SecondaryNavy,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {

                    if (selectedImageUri != null) {
                        AppImage(
                            model = selectedImageUri,
                            contentDescription = "Selected produce image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📷", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Add Produce Image", color = MutedText)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // PRODUCE NAME
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Produce Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = PanelBlue,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // DESCRIPTION
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = PanelBlue,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // CATEGORY
            item {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Vegetables, Fruit)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = PanelBlue,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // PRICE AND UNIT
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (M)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = PanelBlue,
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
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
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Available Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = PanelBlue,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // LOCATION
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showLocationDropdown = true }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select location",
                                    tint = PrimaryBlue
                                )
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
                        expanded = showLocationDropdown,
                        onDismissRequest = { showLocationDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .background(PanelBlue)
                    ) {
                        districts.forEach { district ->
                            DropdownMenuItem(
                                text = { Text(text = district, color = PrimaryText) },
                                onClick = {
                                    location = district
                                    showLocationDropdown = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ADD LISTING BUTTON
            item {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (user != null) {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                var uploadedImageUrl = ""
                                if (selectedImageUri != null) {
                                    val uploadResult = imageRepository.uploadImage(selectedImageUri!!, "produce")
                                    if (uploadResult.isSuccess) {
                                        uploadedImageUrl = uploadResult.getOrDefault("")
                                    } else {
                                        isLoading = false
                                        errorMessage = uploadResult.exceptionOrNull()?.message ?: "Failed to upload image"
                                        return@launch
                                    }
                                }

                                val produce = Produce(
                                    farmerId = user!!.uid,
                                    farmerName = user!!.fullName,
                                    name = name,
                                    description = description,
                                    category = category,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    unit = unit,
                                    quantity = quantity.toIntOrNull() ?: 0,
                                    location = location,
                                    imageUrl = uploadedImageUrl
                                )

                                repository.addProduceListing(produce)
                                    .onSuccess {
                                        isLoading = false
                                        onSuccess()
                                    }
                                    .onFailure {
                                        isLoading = false
                                        errorMessage = it.message ?: "Failed to add produce"
                                    }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = DarkNavy, modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = "Add Listing", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
