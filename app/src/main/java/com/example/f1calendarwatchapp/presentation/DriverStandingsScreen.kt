// DriverStandingsScreen.kt

package com.example.f1calendarwatchapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info // Bilgi ikonu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.*
import com.example.f1calendarwatchapp.data.DriverStanding
import com.example.f1calendarwatchapp.presentation.theme.TeamColors
import com.example.f1calendarwatchapp.presentation.viewmodel.DriverStandingsUiState
import com.example.f1calendarwatchapp.presentation.viewmodel.DriverStandingsViewModel

@Composable
fun DriverStandingsScreen(viewModel: DriverStandingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        when (val state = uiState) {
            is DriverStandingsUiState.Loading -> LoadingView()
            is DriverStandingsUiState.Error -> ErrorView(state.message)

            // YENİ: Boş durum için özel görünüm
            is DriverStandingsUiState.Empty -> SeasonNotStartedView()

            is DriverStandingsUiState.Success -> {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 30.dp, bottom = 40.dp)
                ) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Sürücüler Şampiyonası",
                                style = MaterialTheme.typography.title3,
                                color = Color(0xFFC8F0FF),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "${getCurrentYear()} Sezonu",
                                style = MaterialTheme.typography.caption2,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                    }

                    items(state.standings) { standing ->
                        DriverStandingItem(standing)
                    }
                }
            }
        }
    }
}

// YENİ: Sezon Başlamadı Görünümü
@Composable
fun SeasonNotStartedView() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Bilgi",
                tint = Color.Gray,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Henüz Puan Durumu Yok",
                style = MaterialTheme.typography.title3,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC8F0FF),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sezon başladıktan sonra puan durumu burada listelenecektir.",
                style = MaterialTheme.typography.body2,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DriverStandingItem(standing: DriverStanding) {
    val constructorId = standing.constructors.firstOrNull()?.constructorId ?: "unknown"
    val teamColor = TeamColors.getTeamColor(constructorId)

    // --- PODYUM RENKLERİ ---
    val goldColor = Color(0xFFFFD700)   // Altın
    val silverColor = Color(0xFFC0C0C0) // Gümüş
    val bronzeColor = Color(0xFFCD7F32) // Bronz

    val rankColor = when (standing.position) {
        "1" -> goldColor
        "2" -> silverColor
        "3" -> bronzeColor
        else -> null
    }

    val isPodium = rankColor != null
    val shape = RoundedCornerShape(16.dp)

    Card(
        onClick = { /* Detay yok */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .then(
                if (isPodium) Modifier.border(2.dp, rankColor!!, shape) else Modifier
            ),
        backgroundPainter = CardDefaults.cardBackgroundPainter(Color(0xFF202020)),
        shape = shape
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SOL ŞERİT (Takım Rengi)
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(teamColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // SIRALAMA
            if (isPodium) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Rank ${standing.position}",
                    tint = rankColor!!,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 4.dp)
                )
            } else {
                Text(
                    text = standing.position,
                    style = MaterialTheme.typography.title2,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.width(30.dp)
                )
            }

            // ORTA BİLGİ (İsim Yerine CODE)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val driverNameDisplay = standing.driver.code ?: standing.driver.familyName.uppercase().take(3)

                Text(
                    text = driverNameDisplay,
                    style = if (isPodium) MaterialTheme.typography.title3 else MaterialTheme.typography.body1,
                    fontWeight = if (isPodium) FontWeight.Bold else FontWeight.SemiBold,
                    color = rankColor ?: Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = standing.constructors.firstOrNull()?.name ?: "-",
                    style = MaterialTheme.typography.caption2,
                    color = Color.Gray
                )
            }

            // PUAN
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = standing.points,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold,
                    color = rankColor ?: teamColor
                )
                Text(
                    text = "PTS",
                    style = MaterialTheme.typography.caption2,
                    color = Color.Gray,
                    fontSize = 8.sp
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}