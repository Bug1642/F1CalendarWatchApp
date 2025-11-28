// RaceSession

package com.example.f1calendarwatchapp.data

// Detay ekranında gösterilecek her bir oturumu temsil eder (P1, Quali, Race)
data class RaceSession(
    val id: String,
    val name: String,
    val date: String,
    val time: String?,
    val isCompleted: Boolean
)