// F1CalendarViewModel.kt
package com.example.f1calendarwatchapp.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.f1calendarwatchapp.data.ErgastApi
import com.example.f1calendarwatchapp.data.F1Repository
import com.example.f1calendarwatchapp.data.Race
import com.example.f1calendarwatchapp.data.SessionData
import com.example.f1calendarwatchapp.worker.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime // KRİTİK IMPORT: ZonedDateTime eklendi

// 1. UI Durumlarını Tanımlayan Sealed Class
sealed class CalendarUiState {
    object Loading : CalendarUiState()
    data class Success(val races: List<Race>) : CalendarUiState()
    data class Error(val message: String) : CalendarUiState()
}

// 2. ViewModel Sınıfı
class F1CalendarViewModel(private val repository: F1Repository) : ViewModel() {

    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _nextRace = MutableStateFlow<Race?>(null)
    val nextRace: StateFlow<Race?> = _nextRace.asStateFlow()

    init {
        loadCalendar()
    }

    private fun loadCalendar() {
        viewModelScope.launch {
            try {
                val races = repository.getCalendar()

                findNextRace(races)

                // ==========================================================
                // HATA AYIKLAMA KISMI
                // ==========================================================
                val next = _nextRace.value
                if (next != null) {
                    println("--- DEBUG F1 NEXT RACE DATA ---")
                    println("Yarış Adı: ${next.raceName}")
                    println("API Date String: ${next.date}")
                    println("API Time String (UTC): ${next.time}")

                    val calculatedInstant = next.raceInstant
                    val currentInstant = Instant.now()
                    val duration = Duration.between(currentInstant, calculatedInstant)

                    println("Calculated INSTANT (Countdown Base): $calculatedInstant")
                    println("Current INSTANT (Device): $currentInstant")

                    println("Calculated Duration: ${duration.toDays()} Days, ${duration.toHours() % 24} Hours, ${duration.toMinutes() % 60} Minutes")
                    println("--- END DEBUG ---")
                }
                // ==========================================================

                _uiState.value = CalendarUiState.Success(races)
            } catch (e: Exception) {
                // Eğer hata olursa burayı loglayın!
                println("--- F1 CALENDAR ERROR ---")
                e.printStackTrace()
                println("-------------------------")
                _uiState.value = CalendarUiState.Error(e.localizedMessage ?: "Veri yüklenemedi.")
            }
        }
    }

    private fun findNextRace(races: List<Race>) {
        val now = Instant.now()

        val next = races
            .sortedBy { it.raceInstant }
            .firstOrNull { it.raceInstant.isAfter(now) }

        _nextRace.value = next
    }

    fun scheduleAllFutureRacesNotifications(context: Context) {
        val currentState = _uiState.value as? CalendarUiState.Success ?: return

        val workManager = WorkManager.getInstance(context)
        // Çalışan eski bildirim işlerini iptal et
        workManager.cancelAllWorkByTag("f1_notification_schedule")

        currentState.races.forEach { race ->
            // Sadece gelecekteki yarışların seansları için bildirim planla
            if (race.raceInstant.isBefore(Instant.now().plus(2, ChronoUnit.HOURS))) {
                return@forEach // 2 saatten az kaldıysa veya bittiyse atla
            }

            val allSessions = mutableMapOf<String, SessionData>()
            // Seansları doldurma kısmı
            race.firstPractice?.let { allSessions["${race.raceName} - 1. Antrenman (P1)"] = it }
            race.secondPractice?.let { allSessions["${race.raceName} - 2. Antrenman (P2)"] = it }
            race.thirdPractice?.let { allSessions["${race.raceName} - 3. Antrenman (P3)"] = it }
            race.qualifying?.let { allSessions["${race.raceName} - Sıralama (Quali)"] = it }
            race.sprint?.let { allSessions["${race.raceName} - Sprint Yarışı"] = it }
            allSessions["${race.raceName} - Yarış"] = SessionData(race.date, race.time)

            allSessions.forEach { (sessionName, sessionData) ->
                val timeString = sessionData.time ?: "00:00:00Z"

                // KRİTİK DÜZELTME BAŞLANGIÇ
                val dateTimeString = "${sessionData.date}T$timeString" // Z'yi koru

                val sessionInstant = try {
                    // API'den gelen UTC zaman damgasını (Z ile) doğru parse et.
                    ZonedDateTime.parse(dateTimeString).toInstant()
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@forEach
                }
                // KRİTİK DÜZELTME SONU

                val scheduledInstant = sessionInstant.minus(15, ChronoUnit.MINUTES) // 15 dakika önce
                val currentInstant = Instant.now()

                // Eğer planlanmış zaman şu andan önceyse, atla
                if (scheduledInstant.isBefore(currentInstant) || scheduledInstant.equals(currentInstant)) {
                    return@forEach
                }

                // Gecikme süresi (milisaniye)
                val delayMillis = scheduledInstant.toEpochMilli() - currentInstant.toEpochMilli()

                if (delayMillis < 1000) { // 1 saniyeden az kalanı planlama
                    return@forEach
                }

                val inputData = Data.Builder()
                    .putString(NotificationWorker.KEY_TITLE, "🏎️ YAKLAŞIYOR: $sessionName")
                    .putString(NotificationWorker.KEY_CONTENT, "Seans 15 dakika içinde başlıyor.")
                    .build()

                val uniqueWorkName = "f1_notification_${sessionName.replace(" ", "_").replace("-", "_").replace(".", "").replace("(", "").replace(")", "")}"

                val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInputData(inputData)
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .addTag("f1_notification_schedule")
                    .build()

                workManager.enqueueUniqueWork(
                    uniqueWorkName,
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    notificationWorkRequest
                )
            }
        }
    }

    companion object {
        fun Factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(F1CalendarViewModel::class.java)) {
                    val apiService = ErgastApi.create()
                    return F1CalendarViewModel(
                        repository = F1Repository(api = apiService)
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

}