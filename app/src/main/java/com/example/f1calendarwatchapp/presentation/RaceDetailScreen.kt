package com.example.f1calendarwatchapp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.*
import com.example.f1calendarwatchapp.data.RaceSession
import com.example.f1calendarwatchapp.presentation.theme.CompletedRaceBackground
import com.example.f1calendarwatchapp.presentation.theme.CompletedRaceContent
import com.example.f1calendarwatchapp.presentation.theme.F1Red
import com.example.f1calendarwatchapp.presentation.theme.UpcomingRaceBackground
import com.example.f1calendarwatchapp.presentation.theme.UpcomingRaceContent
import com.example.f1calendarwatchapp.presentation.viewmodel.RaceDetailUiState
import com.example.f1calendarwatchapp.presentation.viewmodel.RaceDetailViewModel

@Composable
fun RaceDetailScreen(
    viewModel: RaceDetailViewModel,
    onSessionClick: (RaceSession) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        timeText = { TimeText() },
    ) {
        when (val state = uiState) {
            RaceDetailUiState.Loading -> LoadingView()
            is RaceDetailUiState.Error -> ErrorView(state.message)
            is RaceDetailUiState.Success -> {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Yarış Adı Başlığı
                    item {
                        Text(
                            text = state.race.raceName,
                            style = MaterialTheme.typography.title3,
                            fontWeight = FontWeight.Bold,
                            color = UpcomingRaceContent,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    // 2. Seans Listesi Başlığı
                    item {
                        Text(
                            text = "Seanslar",
                            style = MaterialTheme.typography.caption1,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    // 3. Seans Listesi
                    items(state.sessions) { session ->
                        SessionItem(
                            session = session,
                            onClick = { onSessionClick(session) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionItem(session: RaceSession, onClick: () -> Unit) {
    val isCompleted = session.isCompleted
    val backgroundColor = if (isCompleted) CompletedRaceBackground else UpcomingRaceBackground
    val contentColor = if (isCompleted) CompletedRaceContent else UpcomingRaceContent.copy(alpha = 0.9f)

    Card(
        onClick = {
            if (isCompleted) onClick()
        },
        enabled = isCompleted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        backgroundPainter = CardDefaults.cardBackgroundPainter(backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // İkon (Solda)
            Icon(
                imageVector = if (session.isCompleted) Icons.Filled.Flag else Icons.Filled.CalendarToday,
                contentDescription = session.name,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Seans Adı (P1, Quali, Race)
                Text(
                    text = session.name,
                    style = MaterialTheme.typography.caption1,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )

                // Tarih ve Saat
                Text(
                    text = formatRaceDateTime(session.date, session.time),
                    style = MaterialTheme.typography.caption3,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }

            // Sağdaki durum metni veya ok işareti
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Detay",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = "Aktif",
                    style = MaterialTheme.typography.caption3,
                    color = F1Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}