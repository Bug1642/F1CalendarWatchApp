// TeamColors.kt

package com.example.f1calendarwatchapp.presentation.theme

import androidx.compose.ui.graphics.Color

object TeamColors {

    // Bilinmeyen takımlar için varsayılan renk (Gri)
    val Default = Color(0xFF808080)

    fun getTeamColor(constructorId: String): Color {
        return when (constructorId.lowercase()) {
            "red_bull" -> Color(0xFF3671C6)      // Red Bull Mavi
            "ferrari" -> Color(0xFFF91536)       // Ferrari Kırmızı
            "mercedes" -> Color(0xFF6CD3BF)      // Mercedes Turkuaz/Gümüş
            "mclaren" -> Color(0xFFF58020)       // McLaren Turuncu
            "aston_martin" -> Color(0xFF225941)  // Aston Martin Yeşil
            "alpine" -> Color(0xFF2293D1)        // Alpine Mavi/Pembe
            "williams" -> Color(0xFF64C4FF)      // Williams Açık Mavi
            "rb" -> Color(0xFF6692FF)            // VCARB (Racing Bulls)
            "sauber" -> Color(0xFF52E252)        // Kick Sauber Yeşil
            "haas" -> Color(0xFFB6BABD)          // Haas Beyaz/Gri
            else -> Default
        }
    }
}