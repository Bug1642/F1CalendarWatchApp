// MainActivity.kt

package com.example.f1calendarwatchapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.f1calendarwatchapp.data.Race
import com.example.f1calendarwatchapp.presentation.theme.F1CalendarWatchAppTheme
import com.example.f1calendarwatchapp.presentation.viewmodel.F1CalendarViewModel
import com.example.f1calendarwatchapp.presentation.viewmodel.RaceDetailViewModel
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.lifecycle.viewmodel.compose.viewModel

// *** İZİN VE BİLDİRİM KODLARI İÇİN GEREKLİ İMPORTLAR ***
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.f1calendarwatchapp.data.ErgastApi
import com.example.f1calendarwatchapp.data.F1Repository
import com.example.f1calendarwatchapp.presentation.viewmodel.CalendarUiState
import com.example.f1calendarwatchapp.presentation.viewmodel.DriverStandingsViewModel
import com.example.f1calendarwatchapp.presentation.viewmodel.RaceDetailUiState
import com.example.f1calendarwatchapp.presentation.viewmodel.SessionResultsViewModel
import com.example.f1calendarwatchapp.presentation.viewmodel.SessionType
import com.example.f1calendarwatchapp.utils.NotificationHelper

// Navigasyon Rotalarını tanımlıyoruz
object Routes {
    const val CALENDAR = "calendar"
    const val DETAIL = "detail/{raceJson}"
    const val DRIVER_STANDINGS = "driver_standings"
    // YENİ ROTA: Sonuçlar (Round ve Tip parametresi alır)
    const val SESSION_RESULTS = "session_results/{round}/{type}"
}

// RaceDetailViewModel'i oluşturmak için Factory
class DetailViewModelFactory(private val race: Race) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RaceDetailViewModel::class.java)) {
            return RaceDetailViewModel(race) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createNotificationChannel(this)

        setContent {
            F1CalendarWatchAppTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val navController = rememberSwipeDismissableNavController()
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Ortak Repository ve API kurulumu
    val apiService = ErgastApi.create()
    val repository = F1Repository(apiService)

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Routes.CALENDAR
    ) {
        // 1. Ana Ekran
        composable(Routes.CALENDAR) {
            val calendarViewModel: F1CalendarViewModel = viewModel(
                factory = F1CalendarViewModel.Factory()
            )
            val uiState by calendarViewModel.uiState.collectAsState()

            LaunchedEffect(uiState) {
                if (uiState is CalendarUiState.Success) {
                    calendarViewModel.scheduleAllFutureRacesNotifications(context)
                }
            }

            F1CalendarScreen(
                viewModel = calendarViewModel,
                onRaceClick = { race ->
                    val raceJson = Gson().toJson(race)
                    val encodedJson = URLEncoder.encode(raceJson, StandardCharsets.UTF_8.toString())
                    navController.navigate("detail/$encodedJson")
                },
                onStandingsClick = {
                    navController.navigate(Routes.DRIVER_STANDINGS)
                }
            )
        }

        // 2. Detay Ekranı
        composable(Routes.DETAIL) { backStackEntry ->
            val raceJson = backStackEntry.arguments?.getString("raceJson")

            if (raceJson != null) {
                val decodedJson = URLDecoder.decode(raceJson, StandardCharsets.UTF_8.toString())
                val race = Gson().fromJson(decodedJson, Race::class.java)

                val detailViewModel = viewModel<RaceDetailViewModel>(
                    factory = DetailViewModelFactory(race)
                )

                // ViewModel'in durumunu izleyerek "round" bilgisini alacağız
                val detailState by detailViewModel.uiState.collectAsState()

                RaceDetailScreen(
                    viewModel = detailViewModel,
                    onSessionClick = { session ->
                        // Sadece Success durumunda ve round bilgisi elimizdeyken işlem yap
                        if (detailState is RaceDetailUiState.Success) {
                            val currentRace = (detailState as RaceDetailUiState.Success).race
                            val round = currentRace.round

                            // Oturum ismine göre tipi belirle
                            val type = when {
                                // GÜNCELLEME: "Shootout" içeriyorsa hemen NULL yap (Tıklanamaz kıl).
                                session.name.contains("Shootout") -> null

                                session.name.contains("Yarış") || session.name.contains("Race") -> SessionType.RACE
                                session.name.contains("Sıralama") || session.name.contains("Qualifying") -> SessionType.QUALIFYING
                                session.name.contains("Sprint") -> SessionType.SPRINT
                                else -> null // Antrenmanlar için null döner.
                            }

                            // Eğer geçerli bir tip varsa (Race, Quali, Sprint) navigasyonu başlat
                            if (type != null) {
                                navController.navigate("session_results/$round/${type.name}")
                            }
                        }
                    }
                )
            } else {
                ErrorView("Yarış Detayı Yüklenemedi.")
            }
        }

        // 3. Sürücüler Puan Durumu
        composable(Routes.DRIVER_STANDINGS) {
            val standingsViewModel: DriverStandingsViewModel = viewModel(
                factory = DriverStandingsViewModel.Factory(repository)
            )
            DriverStandingsScreen(viewModel = standingsViewModel)
        }

        // 4. YENİ: Seans Sonuçları Ekranı
        composable(Routes.SESSION_RESULTS) { backStackEntry ->
            val round = backStackEntry.arguments?.getString("round")
            val typeString = backStackEntry.arguments?.getString("type")

            if (round != null && typeString != null) {
                val sessionType = SessionType.valueOf(typeString)
                val resultsViewModel: SessionResultsViewModel = viewModel(

                    factory = SessionResultsViewModel.Factory(repository, round, sessionType)
                )
                SessionResultsScreen(viewModel = resultsViewModel)
            } else {
                ErrorView("Sonuç bilgisi eksik.")
            }
        }
    }
}