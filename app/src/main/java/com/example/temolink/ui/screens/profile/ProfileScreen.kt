package com.example.temolink.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
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
import com.example.temolink.data.model.User
import com.example.temolink.data.model.UserRole
import com.example.temolink.data.repository.AuthRepository
import com.example.temolink.data.repository.ImageRepository
import com.example.temolink.ui.components.AppImage
import com.example.temolink.ui.theme.*
import com.example.temolink.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val repository = AuthRepository()
    val imageRepository = remember { ImageRepository(context) }

    var isEditing by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var farmName by remember { mutableStateOf("") }
    var farmType by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(user) {
        user?.let {
            fullName = it.fullName
            phone = it.phoneNumber
            location = it.location
            about = it.about
            farmName = it.farmName
            farmType = it.farmType
        }
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("My Profile", color = PrimaryBlue, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(
                            onClick = {
                                isSaving = true
                                scope.launch {
                                    var finalImageUrl = user?.profileImageUrl ?: ""
                                    if (selectedImageUri != null) {
                                        val uploadResult = imageRepository.uploadImage(selectedImageUri!!, "profiles")
                                        if (uploadResult.isSuccess) {
                                            finalImageUrl = uploadResult.getOrDefault(finalImageUrl)
                                        }
                                    }

                                    val updatedUser = user!!.copy(
                                        fullName = fullName,
                                        phoneNumber = phone,
                                        location = location,
                                        about = about,
                                        farmName = farmName,
                                        farmType = farmType,
                                        profileImageUrl = finalImageUrl
                                    )
                                    repository.updateUserProfile(updatedUser)
                                    isSaving = false
                                    isEditing = false
                                }
                            },
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryBlue)
                            } else {
                                Icon(Icons.Default.Save, contentDescription = "Save", tint = PrimaryGreen)
                            }
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { paddingValues ->
        user?.let { userData ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(PanelBlue)
                            .clickable(enabled = isEditing) { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AppImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (userData.profileImageUrl.isNotEmpty()) {
                            AppImage(
                                model = userData.profileImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MutedText, modifier = Modifier.size(32.dp))
                        }
                        
                        if (isEditing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isEditing) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText)
                        )
                    } else {
                        Text(text = userData.fullName, style = MaterialTheme.typography.headlineMedium, color = PrimaryText, fontWeight = FontWeight.Bold)
                        Text(text = userData.role.name, color = PrimaryBlue, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    if (isEditing) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText)
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText)
                        )
                        if (userData.role == UserRole.FARMER) {
                            OutlinedTextField(
                                value = farmName,
                                onValueChange = { farmName = it },
                                label = { Text("Farm Name") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText)
                            )
                            OutlinedTextField(
                                value = farmType,
                                onValueChange = { farmType = it },
                                label = { Text("Farm Type") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText)
                            )
                        }
                        OutlinedTextField(
                            value = about,
                            onValueChange = { about = it },
                            label = { Text("About") },
                            modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText)
                        )
                    } else {
                        ProfileField("Email", userData.email)
                        ProfileField("Phone", userData.phoneNumber)
                        ProfileField("Location", userData.location)
                        
                        if (userData.role == UserRole.FARMER) {
                            ProfileField("Farm Name", userData.farmName)
                            ProfileField("Farm Type", userData.farmType)
                        }
                        
                        ProfileField("About", userData.about)
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                }

                item {
                    if (!isEditing) {
                        Button(
                            onClick = {
                                viewModel.logout()
                                onLogout()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Logout")
                        }
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, color = MutedText, fontSize = 12.sp)
        Text(
            text = value.ifEmpty { "Not specified" },
            color = PrimaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = PanelBlue, thickness = 1.dp)
    }
}
