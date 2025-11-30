// DriverStandingsViewModel.kt
package com.example.f1calendarwatchapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.f1calendarwatchapp.data.DriverStanding
import com.example.f1calendarwatchapp.data.F1Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


// UI Durumları
sealed class DriverStandingsUiState {
    object Loading : DriverStandingsUiState()
    object Empty : DriverStandingsUiState() // YENİ: Boş veri durumu (Sezon başlamadı)
    data class Success(val standings: List<DriverStanding>) : DriverStandingsUiState()
    data class Error(val message: String) : DriverStandingsUiState()
}

class DriverStandingsViewModel(private val repository: F1Repository) : ViewModel() {

    private val _uiState = MutableStateFlow<DriverStandingsUiState>(DriverStandingsUiState.Loading)
    val uiState: StateFlow<DriverStandingsUiState> = _uiState.asStateFlow()

    init {
        loadStandings()
    }

    fun loadStandings() {
        viewModelScope.launch {
            _uiState.value = DriverStandingsUiState.Loading
            try {
                val standings = repository.getDriverStandings()

                if (standings.isEmpty()) {

                    _uiState.value = DriverStandingsUiState.Empty
                } else {
                    _uiState.value = DriverStandingsUiState.Success(standings)
                }
            } catch (e: Exception) {
                _uiState.value = DriverStandingsUiState.Error("Bağlantı hatası: ${e.localizedMessage}")
            }
        }
    }

    // Factory Sınıfı
    class Factory(private val repository: F1Repository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DriverStandingsViewModel::class.java)) {
                return DriverStandingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}