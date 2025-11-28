// RaceDetailViewModel.kt
package com.example.f1calendarwatchapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.f1calendarwatchapp.data.Race
import com.example.f1calendarwatchapp.data.RaceSession
import com.example.f1calendarwatchapp.presentation.isRaceCompleted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

// Detay ekranının durumlarını tanımlarız
sealed class RaceDetailUiState {
    object Loading : RaceDetailUiState()
    data class Success(val race: Race, val sessions: List<RaceSession>) : RaceDetailUiState()
    data class Error(val message: String) : RaceDetailUiState()
}

// Race objesi içindeki seansları işleyen ViewModel
class RaceDetailViewModel(private val race: Race) : ViewModel() {

    private val _uiState = MutableStateFlow<RaceDetailUiState>(RaceDetailUiState.Loading)
    val uiState: StateFlow<RaceDetailUiState> = _uiState.asStateFlow()

    init {
        processRaceSessions()
    }

    private fun processRaceSessions() {
        if (race.raceName.isEmpty()) {
            _uiState.value = RaceDetailUiState.Error("Yarış bilgisi eksik.")
            return
        }

        val sessions = mutableListOf<RaceSession>()

        fun addSession(name: String, date: String, time: String?) {
            if (time != null) {
                sessions.add(
                    RaceSession(
                        id = "${race.raceName.replace(" ", "")}_${name.replace(" ", "")}",
                        name = name,
                        date = date,
                        time = time,
                        isCompleted = isRaceCompleted(date, time)
                    )
                )
            }
        }

        // Seansları (şimdilik) yarış detaylarının geldiği sırayla ekliyoruz
        race.firstPractice?.let { addSession("1. Antrenman (P1)", it.date, it.time) }

        if (race.sprint != null) {
            // Sprint Formatı
            race.qualifying?.let { addSession("Sıralama (Quali)", it.date, it.time) }
            race.secondPractice?.let { addSession("2. Antrenman (P2)", it.date, it.time) }
            race.thirdPractice?.let { addSession("3. Antrenman (P3)", it.date, it.time) }
            race.sprint.let { addSession("Sprint Yarışı", it.date, it.time) }
        } else {
            // Normal Format
            race.secondPractice?.let { addSession("2. Antrenman (P2)", it.date, it.time) }
            race.thirdPractice?.let { addSession("3. Antrenman (P3)", it.date, it.time) }
            race.qualifying?.let { addSession("Sıralama (Quali)", it.date, it.time) }
        }

        // Ana Yarış
        addSession("Yarış (Race)", race.date, race.time)

        // KRONOLOJİK SIRALAMA:
        sessions.sortWith(compareBy { session ->
            val timeString = session.time ?: "00:00:00Z" // Varsayılan UTC saat
            val dateTimeString = "${session.date}T$timeString"

            try {
                // ISO 8601 formatı ile anlık zamanı alıyoruz
                Instant.parse(dateTimeString)
            } catch (e: Exception) {
                // Hata durumunda log'a yazıp en sona atıyoruz.
                e.printStackTrace() // ARTIK 'e' parametresi KULLANILDI.
                Instant.MAX
            }
        })

        _uiState.value = RaceDetailUiState.Success(race, sessions.toList())
    }
}