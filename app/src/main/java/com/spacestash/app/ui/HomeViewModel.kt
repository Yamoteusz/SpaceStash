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

// Reprezentacja wszystkich możliwych stanów naszego ekranu
sealed class HomeUiState {
    object Initial : HomeUiState() // Zanim cokolwiek klikniemy
    object Loading : HomeUiState() // Kółko ładowania
    data class Success(val data: ApodResponse) : HomeUiState() // Gotowe dane z NASA
    data class Error(val message: String) : HomeUiState() // Coś poszło nie tak
}

class HomeViewModel : ViewModel() {
    private val repository = NasaRepository()

    // Obiekt przechowujący aktualny stan ekranu
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Initial)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun fetchApod(apiKey: String) {
        _uiState.value = HomeUiState.Loading // Włączamy ładowanie

        viewModelScope.launch {
            try {
                // Próbujemy pobrać dane z NASA
                val response = repository.getPictureOfTheDay(apiKey)
                _uiState.value = HomeUiState.Success(response) // Sukces!
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Błąd pobierania danych", e)
                _uiState.value = HomeUiState.Error("Brak połączenia lub zły klucz API") // Błąd
            }
        }
    }
}