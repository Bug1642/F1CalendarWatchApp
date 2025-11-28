// F1CalendarScreen.kt
package com.example.f1calendarwatchapp.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.*
import com.example.f1calendarwatchapp.data.Race
import com.example.f1calendarwatchapp.presentation.theme.CompletedRaceBackground
import com.example.f1calendarwatchapp.presentation.theme.CompletedRaceContent
import com.example.f1calendarwatchapp.presentation.theme.UpcomingRaceBackground
import com.example.f1calendarwatchapp.presentation.theme.UpcomingRaceContent
import com.example.f1calendarwatchapp.presentation.viewmodel.CalendarUiState
import com.example.f1calendarwatchapp.presentation.viewmodel.F1CalendarViewModel
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.delay

// ==========================================================
// EKRANLAR
// ==========================================================

@Composable
fun F1CalendarScreen(
    viewModel: F1CalendarViewModel,
    onRaceClick: (Race) -> Unit,
    onStandingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val nextRace by viewModel.nextRace.collectAsState()
    // LocalContext burada bildirim planlaması için kullanılıyor olabilir, koruyoruz.
    val context = LocalContext.current

    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        timeText = { TimeText() },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            CalendarUiState.Loading -> LoadingView()
            is CalendarUiState.Error -> ErrorView(state.message)
            is CalendarUiState.Success -> {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 32.dp, bottom = 48.dp)
                ) {

                    if (nextRace != null) {
                        item {
                            // Geri sayım sayacı
                            CountdownTimer(
                                race = nextRace!!,
                                onClick = onRaceClick
                            )
                        }
                    } else {
                        // Sezon bittiyse mesajı göster
                        item {
                            EndSeasonMessage()
                        }
                    }

                    // Puan Durumu Butonu
                    item {
                        Chip(
                            onClick = onStandingsClick,
                            label = {
                                Text(
                                    text = "Puan Durumu",
                                    style = MaterialTheme.typography.button,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = "Puan Durumu",
                                    tint = Color(0xFFFFD700) // Altın sarısı
                                )
                            },
                            colors = ChipDefaults.secondaryChipColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        Text(
                            text = "${getCurrentYear()} Formula 1 Takvimi",
                            style = MaterialTheme.typography.title3,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )
                    }

                    items(state.races) { race ->
                        RaceItem(race = race, onClick = onRaceClick)
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownTimer(race: Race, onClick: (Race) -> Unit) {
    var currentTime by remember { mutableStateOf(Instant.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Instant.now()
        }
    }

    val duration = Duration.between(currentTime, race.raceInstant)
    val isCompleted = isRaceCompleted(race.date, race.time)

    val countdownText = if (isCompleted) {
        "YARIŞ BİTTİ"
    } else {
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60

        String.format(Locale.ROOT, "%dG %02d:%02d:%02d", days, hours, minutes, seconds)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .clickable(onClick = { onClick(race) }) // Tıklanabilir
            .background(
                color = MaterialTheme.colors.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isCompleted) "Sonuçlar için TIKLA" else "Sıradaki: ${race.raceName}",
            style = MaterialTheme.typography.caption2,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = countdownText,
            style = MaterialTheme.typography.title1,
            fontWeight = FontWeight.Bold,
            color = if (isCompleted) CompletedRaceContent else Color(0xFFC8F0FF)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = formatRaceDateTime(race.date, race.time),
            style = MaterialTheme.typography.caption2,
            color = Color.Gray.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun EndSeasonMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Flag,
            contentDescription = "Sezon Bitti",
            tint = Color(0xFFC8F0FF),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "🏁 SEZON BİTTİ 🏁",
            style = MaterialTheme.typography.title3,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC8F0FF)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Yeni sezon takvimi bekleniyor.",
            style = MaterialTheme.typography.caption1,
            color = Color.Gray
        )
    }
}

@Composable
fun RaceItem(race: Race, onClick: (Race) -> Unit) {
    val isCompleted = isRaceCompleted(race.date, race.time)
    val backgroundColor = if (isCompleted) CompletedRaceBackground else UpcomingRaceBackground
    val contentColor = if (isCompleted) CompletedRaceContent else UpcomingRaceContent

    // DÜZELTME: Artık tüm yarışlar tıklanabilir (Sonuçları görmek için)
    Card(
        onClick = { onClick(race) }, // Koşulsuz tıklama
        enabled = true,             // Her zaman aktif
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        backgroundPainter = CardDefaults.cardBackgroundPainter(backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp)
                    .weight(1f)
            ) {
                // Yarış Adı
                Text(
                    text = race.raceName,
                    style = MaterialTheme.typography.title3,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Konum
                Text(
                    text = race.circuit.location.country,
                    style = MaterialTheme.typography.caption1,
                    color = contentColor.copy(alpha = 0.6f)
                )

                // Tarih ve Saat
                Text(
                    text = formatRaceDateTime(race.date, race.time),
                    style = MaterialTheme.typography.caption1,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) CompletedRaceContent.copy(alpha = 0.9f) else Color(0xFFC8F0FF),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // İkon
            Icon(
                imageVector = if (isCompleted) Icons.Filled.Flag else Icons.Filled.Timelapse,
                contentDescription = if (isCompleted) "Tamamlandı" else "Yaklaşan",
                tint = if (isCompleted) CompletedRaceContent else Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.Top)
            )
        }
    }
}