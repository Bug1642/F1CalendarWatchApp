// CommonUI.kt
package com.example.f1calendarwatchapp.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.wear.compose.material.Text // KRİTİK EKLENTİ
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.CircularProgressIndicator // KRİTİK EKLENTİ
// Eğer Android Studio, Box ve fillMaxSize için ek importlar isterse onları da ekleyin,
// ancak genellikle androidx.compose.foundation.layout'tan gelirler.

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Hata: $message", color = Color.Red)
    }
}