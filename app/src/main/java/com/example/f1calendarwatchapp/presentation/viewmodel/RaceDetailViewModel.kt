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
import java.time.ZonedDateTime

sealed class RaceDetailUiState {
    object Loading : RaceDetailUiState()
    data class Success(val race: Race, val sessions: List<RaceSession>) : RaceDetailUiState()
    data class Error(val message: String) : RaceDetailUiState()
}

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
            val sessionTime = time ?: "00:00:00Z"
            sessions.add(
                RaceSession(
                    id = name.replace(" ", ""), // ID oluşturma basitleştirildi
                    name = name,
                    date = date,
                    time = sessionTime,
                    isCompleted = isRaceCompleted(date, sessionTime)
                )
            )
        }

        // --- SEANSLARI EKLEME ---

        // 1. Antrenman (Her zaman var)
        race.firstPractice?.let { addSession("1. Antrenman (P1)", it.date, it.time) }

        // 2. Antrenman
        race.secondPractice?.let { addSession("2. Antrenman (P2)", it.date, it.time) }

        // 3. Antrenman
        race.thirdPractice?.let { addSession("3. Antrenman (P3)", it.date, it.time) }

        // *** YENİ KISIM: Sprint Qualifying (Shootout) ***
        race.sprintQualifying?.let {
            addSession("Sprint Shootout", it.date, it.time)
        }

        // Sprint Yarışı
        race.sprint?.let { addSession("Sprint Yarışı", it.date, it.time) }

        // Sıralama Turları (Ana Yarış İçin)
        race.qualifying?.let { addSession("Sıralama (Quali)", it.date, it.time) }

        // Ana Yarış
        addSession("Yarış (Race)", race.date, race.time)

        // --- SIRALAMA ---
        // Listeyi kronolojik olarak (tarih/saat sırasına göre) yeniden düzenliyoruz.
        // Böylece Shootout Cuma günü mü, Cumartesi mi doğru yerde durur.
        sessions.sortWith(compareBy { session ->
            val timeString = session.time ?: "00:00:00Z"
            val dateTimeString = "${session.date}T$timeString"
            try {
                // ZonedDateTime kullanmak Instant.parse'dan daha güvenlidir
                ZonedDateTime.parse(dateTimeString).toInstant()
            } catch (e: Exception) {
                // Hata durumunda en sona at
                Instant.MAX
            }
        })

        _uiState.value = RaceDetailUiState.Success(race, sessions)
    }
}