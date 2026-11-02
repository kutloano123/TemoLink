package com.example.temolink.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temolink.data.model.Order
import com.example.temolink.data.model.OrderStatus
import com.example.temolink.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OrderState {
    object Loading : OrderState()
    data class Success(val orders: List<Order>) : OrderState()
    data class Error(val message: String) : OrderState()
}

class OrderViewModel(private val repository: OrderRepository = OrderRepository()) : ViewModel() {

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Loading)
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    fun loadBuyerOrders(buyerId: String) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            val orders = repository.getOrdersForBuyer(buyerId)
            _orderState.value = OrderState.Success(orders)
        }
    }

    fun loadFarmerOrders(farmerId: String) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            val orders = repository.getOrdersForFarmer(farmerId)
            _orderState.value = OrderState.Success(orders)
        }
    }

    fun placeOrder(order: Order, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.createOrder(order)
                .onSuccess { onSuccess() }
        }
    }

    fun updateStatus(orderId: String, status: OrderStatus, onRefresh: () -> Unit) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
                .onSuccess { onRefresh() }
        }
    }
}
