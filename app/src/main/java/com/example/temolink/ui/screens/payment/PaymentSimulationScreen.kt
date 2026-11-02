package com.example.temolink.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.temolink.data.model.Order
import com.example.temolink.data.model.OrderStatus
import com.example.temolink.ui.theme.*
import com.example.temolink.ui.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSimulationScreen(
    produceId: String,
    quantity: Int,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    marketplaceViewModel: MarketplaceViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel()
) {
    val detailsState by marketplaceViewModel.detailsState.collectAsState()
    val user by authViewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedMethod by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var paymentStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(produceId) {
        marketplaceViewModel.loadProduceDetails(produceId)
        user?.let { phoneNumber = it.phoneNumber }
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("Payment Simulation", color = PrimaryBlue, fontWeight = FontWeight.Bold) },
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
            is ProduceDetailsState.Success -> {
                val produce = state.produce
                val totalAmount = produce.price * quantity

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Order Summary", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    Text(text = "${produce.name} x $quantity", color = PrimaryText)
                    Text(
                        text = "Total: M$totalAmount",
                        color = PrimaryGreen,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    
                    HorizontalDivider(color = SecondaryNavy, modifier = Modifier.padding(vertical = 16.dp))
                    
                    Text(
                        text = "Select Payment Method",
                        color = PrimaryText,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    PaymentMethodItem("Mpesa", selectedMethod == "Mpesa") { selectedMethod = "Mpesa" }
                    Spacer(modifier = Modifier.height(8.dp))
                    PaymentMethodItem("Echo Cash", selectedMethod == "Echo Cash") { selectedMethod = "Echo Cash" }
                    
                    if (selectedMethod.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PrimaryText,
                                unfocusedTextColor = PrimaryText,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = PanelBlue
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                isProcessing = true
                                scope.launch {
                                    paymentStatus = "Waiting for PIN entry on your phone..."
                                    delay(3000)
                                    paymentStatus = "Processing..."
                                    delay(2000)
                                    
                                    val order = Order(
                                        buyerId = user!!.uid,
                                        buyerName = user!!.fullName,
                                        farmerId = produce.farmerId,
                                        produceId = produce.produceId,
                                        produceName = produce.name,
                                        quantity = quantity,
                                        price = produce.price,
                                        totalPrice = totalAmount,
                                        status = OrderStatus.CONFIRMED
                                    )
                                    
                                    orderViewModel.placeOrder(order) {
                                        paymentStatus = "Transaction Successful!"
                                        scope.launch {
                                            delay(2000)
                                            onSuccess()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isProcessing && phoneNumber.length >= 8
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = DarkNavy, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Pay Now", fontWeight = FontWeight.Bold, color = DarkNavy)
                            }
                        }
                    }
                    
                    paymentStatus?.let {
                        Text(
                            text = it,
                            color = if (it.contains("Successful")) PrimaryGreen else PrimaryBlue,
                            modifier = Modifier.padding(top = 24.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            is ProduceDetailsState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error loading produce details", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun PaymentMethodItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else PanelBlue,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) PrimaryBlue else SecondaryNavy
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected, 
                onClick = onClick, 
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = name, color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
