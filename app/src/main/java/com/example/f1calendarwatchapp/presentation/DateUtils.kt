// DateUtils.kt
package com.example.f1calendarwatchapp.presentation

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Locale
// KRİTİK İMPORTLAR
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

// ==========================================================
// TARİH VE SAAT YARDIMCI FONKSİYONLARI
// ==========================================================

/**
 * Yarış zamanının geçip geçmediğini kontrol eder.
 * Yarışın başlangıcından 2 saat sonra tamamlanmış sayılır.
 */
fun isRaceCompleted(raceDate: String, raceTime: String?): Boolean {
    val rawTimeString = raceTime ?: "00:00:00Z"
    // "Z"yi kaldırarak sadece yerel tarih/saat kısmı kalır (Repository ile tutarlı)
    val dateTimeString = "${raceDate}T${rawTimeString.removeSuffix("Z")}"

    return try {
        // 1. LocalDateTime olarak parse et (Repository ile aynı başlangıç noktası)
        val localDateTime = LocalDateTime.parse(dateTimeString)

        // 2. Açıkça UTC saat dilimine göre Instant'ı al
        val raceInstant = localDateTime.toInstant(ZoneOffset.UTC)

        // 3. Yarışın BİTTİ sayılması için 2 saat ekle (Tampon zaman)
        val completionInstant = raceInstant.plus(2, ChronoUnit.HOURS)

        // 4. Yarış bitiş zamanı (2 saat eklenmiş hali) şu andan önce mi?
        completionInstant.isBefore(Instant.now())
    } catch (e: Exception) {
        // Hata durumunda tamamlanmamış say.
        false
    }
}

/**
 * Yarış tarih ve saatini cihazın yerel ayarlarına göre biçimlendirir.
 */
fun formatRaceDateTime(raceDate: String, raceTime: String?): String {
    val timeString = raceTime ?: "00:00:00Z"
    val dateTimeString = "${raceDate}T$timeString"

    return try {
        // ZonedDateTime ile parse ederek UTC bilgisini al ve cihazın yerel saatine dönüştür
        val raceDateTime = ZonedDateTime.parse(dateTimeString)
            .withZoneSameInstant(ZoneId.systemDefault())

        // Cihazın varsayılan yerel ayarını kullanarak zaman formatlayıcılarını oluştur
        val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())

        // Ay adının kısaltılmışını ve günü gösterir (örn: 20 Kas)
        val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

        val shortFormattedDate = raceDateTime.format(shortDateFormatter)
        val formattedTime = raceDateTime.format(timeFormatter)

        "$shortFormattedDate, $formattedTime"

    } catch (e: Exception) {
        // Hata durumunda sadece tarihi göster.
        raceDate
    }
}

// Yıl bilgisini döndürür (Takvim başlığı için)
fun getCurrentYear(): Int {
    return Calendar.getInstance().get(Calendar.YEAR)
}