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
fun BuyerOrdersScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel()
) {
    val user by authViewModel.currentUser.collectAsState()
    val orderState by orderViewModel.orderState.collectAsState()

    LaunchedEffect(user) {
        user?.let { orderViewModel.loadBuyerOrders(it.uid) }
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("My Orders", color = PrimaryBlue, fontWeight = FontWeight.Bold) },
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
                        Text("No orders placed yet.", color = SecondaryText)
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
                            BuyerOrderCard(order)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuyerOrderCard(order: Order) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    
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
                Text(
                    text = order.produceName,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                OrderStatusChip(order.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Order ID: ${order.orderId}",
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Date: ${dateFormat.format(Date(order.createdAt))}",
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = SecondaryNavy)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Quantity", color = MutedText, fontSize = 12.sp)
                    Text(text = "${order.quantity}", color = PrimaryText, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Price", color = MutedText, fontSize = 12.sp)
                    Text(text = "M${order.totalPrice}", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: OrderStatus) {
    val color = when (status) {
        OrderStatus.PENDING -> Color(0xFFEAB308) // Yellow
        OrderStatus.CONFIRMED -> PrimaryBlue
        OrderStatus.COMPLETED -> PrimaryGreen
        OrderStatus.CANCELLED -> Color.Red
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status.name,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
