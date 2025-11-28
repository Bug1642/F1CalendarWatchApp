// SessionResultsScreen.kt

package com.example.f1calendarwatchapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.*
import com.example.f1calendarwatchapp.data.BaseResult
import com.example.f1calendarwatchapp.presentation.theme.TeamColors
import com.example.f1calendarwatchapp.presentation.viewmodel.SessionResultsUiState
import com.example.f1calendarwatchapp.presentation.viewmodel.SessionResultsViewModel
import com.example.f1calendarwatchapp.presentation.viewmodel.SessionType

@Composable
fun SessionResultsScreen(viewModel: SessionResultsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        when (val state = uiState) {
            is SessionResultsUiState.Loading -> LoadingView()
            is SessionResultsUiState.Error -> ErrorView(state.message)
            is SessionResultsUiState.Success -> {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 30.dp, bottom = 40.dp)
                ) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = getTitleForSession(state.sessionType),
                                style = MaterialTheme.typography.title3,
                                color = Color(0xFFC8F0FF),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Sonuçlar",
                                style = MaterialTheme.typography.caption2,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                    }

                    items(state.results) { result ->
                        ResultItem(result)
                    }
                }
            }
        }
    }
}

fun getTitleForSession(type: SessionType): String {
    return when (type) {
        SessionType.RACE -> "Yarış Sonucu"
        SessionType.QUALIFYING -> "Sıralama Turları"
        SessionType.SPRINT -> "Sprint Yarışı"
    }
}

@Composable
fun ResultItem(result: BaseResult) {
    val teamColor = TeamColors.getTeamColor(result.constructor.constructorId)

    // --- PODYUM RENKLERİ ---
    val goldColor = Color(0xFFFFD700)
    val silverColor = Color(0xFFC0C0C0)
    val bronzeColor = Color(0xFFCD7F32)

    val rankColor = when (result.position) {
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
                    contentDescription = "Rank ${result.position}",
                    tint = rankColor!!,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 4.dp)
                )
            } else {
                Text(
                    text = result.position,
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
                // --- DEĞİŞİKLİK BURADA ---
                val driverNameDisplay = result.driver.code ?: result.driver.familyName.uppercase().take(3)

                Text(
                    text = driverNameDisplay,
                    style = if (isPodium) MaterialTheme.typography.title3 else MaterialTheme.typography.body1,
                    fontWeight = if (isPodium) FontWeight.Bold else FontWeight.SemiBold,
                    color = rankColor ?: Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = result.constructor.name,
                    style = MaterialTheme.typography.caption2,
                    color = Color.Gray
                )
            }

            // ZAMAN / PUAN
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = result.timeOrStatus,
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 10.sp
                )
                if (!result.points.isNullOrEmpty() && result.points != "0") {
                    Text(
                        text = "+${result.points} PTS",
                        style = MaterialTheme.typography.caption2,
                        color = rankColor ?: teamColor,
                        fontSize = 9.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}