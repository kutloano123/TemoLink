package com.example.temolink.ui.screens.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.temolink.ui.viewmodel.AuthViewModel
import com.example.temolink.ui.viewmodel.OrderState
import com.example.temolink.ui.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerOrdersScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel()
) {
    val user by authViewModel.currentUser.collectAsState()
    val orderState by orderViewModel.orderState.collectAsState()

    LaunchedEffect(user) {
        user?.let { orderViewModel.loadFarmerOrders(it.uid) }
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("Manage Orders", color = PrimaryBlue, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { paddingValues ->
        when (val state = orderState) {
            is OrderState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            is OrderState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = Color.Red)
                }
            }
            is OrderState.Success -> {
                if (state.orders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No orders received yet.", color = SecondaryText)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.orders) { order ->
                            FarmerOrderCard(order) { newStatus ->
                                orderViewModel.updateStatus(order.orderId, newStatus) {
                                    user?.let { orderViewModel.loadFarmerOrders(it.uid) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerOrderCard(order: Order, onUpdateStatus: (OrderStatus) -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PanelBlue),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = order.produceName,
                        color = PrimaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(text = "Buyer: ${order.buyerName}", color = PrimaryBlue, fontSize = 14.sp)
                }
                OrderStatusChip(order.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Quantity: ${order.quantity}", color = SecondaryText, fontSize = 14.sp)
                    Text(text = "Total: M${order.totalPrice}", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = dateFormat.format(Date(order.createdAt)),
                    color = MutedText,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (order.status != OrderStatus.COMPLETED && order.status != OrderStatus.CANCELLED) {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryNavy),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Update Status", color = PrimaryBlue)
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Update Order Status") },
            text = { Text("Choose a new status for this order.") },
            confirmButton = {},
            dismissButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (order.status == OrderStatus.PENDING) {
                        DropdownMenuItem(
                            text = { Text("Confirm Order") },
                            onClick = { onUpdateStatus(OrderStatus.CONFIRMED); showDialog = false }
                        )
                    }
                    if (order.status == OrderStatus.CONFIRMED) {
                        DropdownMenuItem(
                            text = { Text("Mark as Completed") },
                            onClick = { onUpdateStatus(OrderStatus.COMPLETED); showDialog = false }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Cancel Order") },
                        onClick = { onUpdateStatus(OrderStatus.CANCELLED); showDialog = false }
                    )
                    TextButton(
                        onClick = { showDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Dismiss")
                    }
                }
            },
            containerColor = PanelBlue,
            titleContentColor = PrimaryText,
            textContentColor = SecondaryText
        )
    }
}
