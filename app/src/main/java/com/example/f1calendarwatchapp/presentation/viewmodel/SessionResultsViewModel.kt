// SessionResultsViewModel.kt
package com.example.f1calendarwatchapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.f1calendarwatchapp.data.BaseResult
import com.example.f1calendarwatchapp.data.F1Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Hangi sonuç tipini istiyoruz?
enum class SessionType {
    RACE,
    QUALIFYING,
    SPRINT
}

// UI Durumları
sealed class SessionResultsUiState {
    object Loading : SessionResultsUiState()
    data class Success(val results: List<BaseResult>, val sessionType: SessionType) : SessionResultsUiState()
    data class Error(val message: String) : SessionResultsUiState()
}

class SessionResultsViewModel(
    private val repository: F1Repository,
    private val round: String,
    private val sessionType: SessionType
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionResultsUiState>(SessionResultsUiState.Loading)
    val uiState: StateFlow<SessionResultsUiState> = _uiState.asStateFlow()

    init {
        loadResults()
    }

    fun loadResults() {
        viewModelScope.launch {
            _uiState.value = SessionResultsUiState.Loading
            try {
                val results = when (sessionType) {
                    SessionType.RACE -> repository.getRaceResults(round)
                    SessionType.QUALIFYING -> repository.getQualifyingResults(round)
                    SessionType.SPRINT -> repository.getSprintResults(round)
                }

                if (results.isEmpty()) {
                    _uiState.value = SessionResultsUiState.Error("Sonuçlar henüz mevcut değil.")
                } else {
                    _uiState.value = SessionResultsUiState.Success(results, sessionType)
                }
            } catch (e: Exception) {
                _uiState.value = SessionResultsUiState.Error("Bağlantı hatası: ${e.localizedMessage}")
            }
        }
    }

    // Factory Sınıfı (Parametreleri alabilmek için)
    class Factory(
        private val repository: F1Repository,
        private val round: String,
        private val sessionType: SessionType
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SessionResultsViewModel::class.java)) {
                return SessionResultsViewModel(repository, round, sessionType) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}