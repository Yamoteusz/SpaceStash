package com.spacestash.app.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spacestash.app.data.NasaRepository
import com.spacestash.app.domain.ApodResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Initial : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val data: ApodResponse) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {
    private val repository = NasaRepository()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Initial)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun fetchApod(apiKey: String) {
        _uiState.value = HomeUiState.Loading

        viewModelScope.launch {
            try {
                val response = repository.getPictureOfTheDay(apiKey)
                _uiState.value = HomeUiState.Success(response)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Błąd pobierania danych", e)
                _uiState.value = HomeUiState.Error("Brak połączenia lub zły klucz API")
            }
        }
    }
}