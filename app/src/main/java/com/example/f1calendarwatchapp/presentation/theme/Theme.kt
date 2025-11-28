// Theme.kt

package com.example.f1calendarwatchapp.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.MaterialTheme

// Özel Renklerimizi Tanımlama

val CompletedRaceBackground = Color(0xFF151515)
val CompletedRaceContent = Color(0xFF888888)

val UpcomingRaceBackground = Color(0xFF00375E)
val UpcomingRaceContent = Color(Color.White.value)

val F1Red = Color(0xFFFF1E00)

@Composable
fun F1CalendarWatchAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = UpcomingRaceBackground,
            secondary = F1Red,
            background = Color.Black
        ),
        content = content
    )
}