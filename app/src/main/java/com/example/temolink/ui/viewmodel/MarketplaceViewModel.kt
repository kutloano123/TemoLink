package com.example.temolink.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temolink.data.model.Produce
import com.example.temolink.data.model.User
import com.example.temolink.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class MarketplaceState {
    object Loading : MarketplaceState()
    data class Success(val produceList: List<Produce>) : MarketplaceState()
    data class Error(val message: String) : MarketplaceState()
}

sealed class ProduceDetailsState {
    object Idle : ProduceDetailsState()
    object Loading : ProduceDetailsState()
    data class Success(val produce: Produce, val farmer: User) : ProduceDetailsState()
    data class Error(val message: String) : ProduceDetailsState()
}

class MarketplaceViewModel(private val repository: MarketplaceRepository = MarketplaceRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<MarketplaceState>(MarketplaceState.Loading)
    val uiState: StateFlow<MarketplaceState> = _uiState.asStateFlow()

    private val _detailsState = MutableStateFlow<ProduceDetailsState>(ProduceDetailsState.Idle)
    val detailsState: StateFlow<ProduceDetailsState> = _detailsState.asStateFlow()

    init {
        observeMarketplace()
    }

    private fun observeMarketplace() {
        viewModelScope.launch {
            _uiState.value = MarketplaceState.Loading
            repository.observeAllProduce()
                .catch { e ->
                    _uiState.value = MarketplaceState.Error(e.message ?: "Failed to load marketplace")
                }
                .collect { list ->
                    _uiState.value = MarketplaceState.Success(list)
                }
        }
    }

    fun loadProduceDetails(produceId: String) {
        viewModelScope.launch {
            _detailsState.value = ProduceDetailsState.Loading
            try {
                val produce = repository.getProduceById(produceId)
                if (produce != null) {
                    val farmer = repository.getFarmerPublicProfile(produce.farmerId)
                    if (farmer != null) {
                        _detailsState.value = ProduceDetailsState.Success(produce, farmer)
                    } else {
                        _detailsState.value = ProduceDetailsState.Error("Farmer not found")
                    }
                } else {
                    _detailsState.value = ProduceDetailsState.Error("Produce not found")
                }
            } catch (e: Exception) {
                _detailsState.value = ProduceDetailsState.Error(e.message ?: "Failed to load details")
            }
        }
    }
}
