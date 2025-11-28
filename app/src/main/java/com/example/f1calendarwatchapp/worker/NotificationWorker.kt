// NotificationWorker
package com.example.f1calendarwatchapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.f1calendarwatchapp.utils.NotificationHelper // NotificationHelper'ı import edin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager tarafından çalıştırılan arka plan işi.
 */
class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    // InputData anahtarları
    companion object {
        const val KEY_TITLE = "title"
        const val KEY_CONTENT = "content"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Test butonu veya planlanmış işten gelen verileri alıyoruz
            val title = inputData.getString(KEY_TITLE) ?: "F1 Bildirimi"
            val content = inputData.getString(KEY_CONTENT) ?: "Yaklaşan seans başlıyor."

            // Bildirimi gösteren yardımcı fonksiyonu çağır
            NotificationHelper.showNotification(applicationContext, title, content)

            // İş başarılı
            Result.success()
        } catch (e: Exception) {
            // Herhangi bir hata durumunda (örneğin izin eksikliği)
            e.printStackTrace()
            Result.failure()
        }
    }
}