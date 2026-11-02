package com.example.temolink.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.temolink.data.model.Produce
import com.example.temolink.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FarmerProduceState {
    object Loading : FarmerProduceState()
    data class Success(val listings: List<Produce>) : FarmerProduceState()
    data class Error(val message: String) : FarmerProduceState()
}

class FarmerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MarketplaceRepository()

    private val _uiState = MutableStateFlow<FarmerProduceState>(FarmerProduceState.Loading)
    val uiState: StateFlow<FarmerProduceState> = _uiState.asStateFlow()

    fun loadListings(farmerId: String) {
        viewModelScope.launch {
            _uiState.value = FarmerProduceState.Loading
            try {
                val list = repository.getProduceByFarmer(farmerId)
                _uiState.value = FarmerProduceState.Success(list)
            } catch (e: Exception) {
                _uiState.value = FarmerProduceState.Error(e.message ?: "Failed to load listings")
            }
        }
    }

    fun deleteListing(produceId: String, farmerId: String) {
        viewModelScope.launch {
            repository.deleteProduceListing(produceId)
                .onSuccess { loadListings(farmerId) }
        }
    }
}
