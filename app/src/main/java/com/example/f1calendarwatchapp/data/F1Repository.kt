// F1Repository.kt

package com.example.f1calendarwatchapp.data

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.Calendar
import java.time.Instant
import java.time.ZonedDateTime
// SSL Sorununu Aşmak İçin Gerekli Importlar
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.util.concurrent.TimeUnit

// --- 1. API Servisi Arayüzü (Retrofit) ---
interface ErgastApi {
    @GET("{year}.json")
    suspend fun getRaces(@Path("year") year: Int): ErgastResponse

    // Sürücü Puan Durumu Endpoint'i
    @GET("{year}/driverStandings.json")
    suspend fun getDriverStandings(@Path("year") year: Int): DriverStandingsResponse

    // YENİ: Yarış Sonuçları (Race Results)
    @GET("{year}/{round}/results.json")
    suspend fun getRaceResults(
        @Path("year") year: String,
        @Path("round") round: String
    ): RaceResultsResponse

    // YENİ: Sıralama Sonuçları (Qualifying Results)
    @GET("{year}/{round}/qualifying.json")
    suspend fun getQualifyingResults(
        @Path("year") year: String,
        @Path("round") round: String
    ): QualifyingResultsResponse

    // YENİ: Sprint Sonuçları (Sprint Results)
    @GET("{year}/{round}/sprint.json")
    suspend fun getSprintResults(
        @Path("year") year: String,
        @Path("round") round: String
    ): SprintResultsResponse

    companion object {
        private const val BASE_URL = "https://api.jolpi.ca/ergast/f1/"

        fun create(): ErgastApi {
            val unsafeClient = getUnsafeOkHttpClient()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(unsafeClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ErgastApi::class.java)
        }

        private fun getUnsafeOkHttpClient(): OkHttpClient {
            return try {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })

                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                val sslSocketFactory = sslContext.socketFactory

                OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                    .hostnameVerifier { _, _ -> true }
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}

// --- 2. Data Sınıfları (Yarış Takvimi İçin) ---
data class ErgastResponse(@SerializedName("MRData") val mrData: MRData)
data class MRData(@SerializedName("RaceTable") val raceTable: RaceTable)
data class RaceTable(@SerializedName("Races") val races: List<RaceData>)
data class RaceData(
    @SerializedName("round") val round: String, // Round eklendi, sonuçları çekmek için gerekli
    @SerializedName("raceName") val raceName: String,
    @SerializedName("Circuit") val circuit: Circuit,
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: String?,
    @SerializedName("FirstPractice") val firstPractice: SessionData?,
    @SerializedName("SecondPractice") val secondPractice: SessionData?,
    @SerializedName("ThirdPractice") val thirdPractice: SessionData?,
    @SerializedName("Qualifying") val qualifying: SessionData?,
    @SerializedName("Sprint") val sprint: SessionData?,
    @SerializedName("SprintQualifying") val sprintQualifying: SessionData? // <-- YENİ ALAN
)
data class Circuit(
    @SerializedName("circuitName") val circuitName: String,
    @SerializedName("Location") val location: Location
)
data class Location(
    @SerializedName("country") val country: String
)
data class SessionData(
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: String?
)
data class Race(
    val round: String,
    val raceName: String,
    val circuit: Circuit,
    val date: String,
    val time: String?,
    val firstPractice: SessionData?,
    val secondPractice: SessionData?,
    val thirdPractice: SessionData?,
    val qualifying: SessionData?,
    val sprint: SessionData?,
    val raceInstant: Instant,
    val sprintQualifying: SessionData?, // <-- YENİ ALAN
)

// --- 3. DATA SINIFLARI (Puan Durumu İçin) ---

data class DriverStandingsResponse(@SerializedName("MRData") val mrData: StandingsMRData)
data class StandingsMRData(@SerializedName("StandingsTable") val standingsTable: StandingsTable)
data class StandingsTable(@SerializedName("StandingsLists") val standingsLists: List<StandingsList>)
data class StandingsList(@SerializedName("DriverStandings") val driverStandings: List<DriverStanding>)

data class DriverStanding(
    @SerializedName("position") val position: String,
    @SerializedName("points") val points: String,
    @SerializedName("wins") val wins: String,
    @SerializedName("Driver") val driver: Driver,
    @SerializedName("Constructors") val constructors: List<Constructor>
)

data class Driver(
    @SerializedName("driverId") val driverId: String,
    @SerializedName("givenName") val givenName: String,
    @SerializedName("familyName") val familyName: String,
    @SerializedName("code") val code: String? // Örn: VER, HAM
)

data class Constructor(
    @SerializedName("constructorId") val constructorId: String,
    @SerializedName("name") val name: String
)

// --- 4. DATA SINIFLARI (Sonuçlar İçin - Yarış, Sıralama, Sprint) ---

// Ortak bir Sonuç Arayüzü (UI'da kolay göstermek için)
interface BaseResult {
    val position: String
    val driver: Driver
    val constructor: Constructor
    val timeOrStatus: String // Süre veya Durum (+10.00s, DNF, vb.)
    val points: String? // Sıralamada puan olmayabilir
}

// -- Yarış Sonuçları --
data class RaceResultsResponse(@SerializedName("MRData") val mrData: RaceResultsMRData)
data class RaceResultsMRData(@SerializedName("RaceTable") val raceTable: RaceResultsTable)
data class RaceResultsTable(@SerializedName("Races") val races: List<RaceResultData>)
data class RaceResultData(@SerializedName("Results") val results: List<RaceResultItem>)

data class RaceResultItem(
    @SerializedName("position") override val position: String,
    @SerializedName("points") override val points: String?,
    @SerializedName("Driver") override val driver: Driver,
    @SerializedName("Constructor") override val constructor: Constructor,
    @SerializedName("Time") val time: TimeData?,
    @SerializedName("status") val status: String
) : BaseResult {
    override val timeOrStatus: String
        get() = time?.time ?: status
}
data class TimeData(@SerializedName("time") val time: String)

// -- Sıralama (Qualifying) Sonuçları --
data class QualifyingResultsResponse(@SerializedName("MRData") val mrData: QualifyingResultsMRData)
data class QualifyingResultsMRData(@SerializedName("RaceTable") val raceTable: QualifyingResultsTable)
data class QualifyingResultsTable(@SerializedName("Races") val races: List<QualifyingResultData>)
data class QualifyingResultData(@SerializedName("QualifyingResults") val results: List<QualifyingResultItem>)

data class QualifyingResultItem(
    @SerializedName("position") override val position: String,
    @SerializedName("Driver") override val driver: Driver,
    @SerializedName("Constructor") override val constructor: Constructor,
    @SerializedName("Q1") val q1: String?,
    @SerializedName("Q2") val q2: String?,
    @SerializedName("Q3") val q3: String?
) : BaseResult {
    override val points: String? = null // Sıralamada doğrudan puan API'de dönmez
    override val timeOrStatus: String
        get() = q3 ?: q2 ?: q1 ?: "No Time"
}

// -- Sprint Sonuçları --
data class SprintResultsResponse(@SerializedName("MRData") val mrData: SprintResultsMRData)
data class SprintResultsMRData(@SerializedName("RaceTable") val raceTable: SprintResultsTable)
data class SprintResultsTable(@SerializedName("Races") val races: List<SprintResultData>)
data class SprintResultData(@SerializedName("SprintResults") val results: List<SprintResultItem>)

data class SprintResultItem(
    @SerializedName("position") override val position: String,
    @SerializedName("points") override val points: String?,
    @SerializedName("Driver") override val driver: Driver,
    @SerializedName("Constructor") override val constructor: Constructor,
    @SerializedName("Time") val time: TimeData?,
    @SerializedName("status") val status: String
) : BaseResult {
    override val timeOrStatus: String
        get() = time?.time ?: status
}


// --- 5. Repository Sınıfı ---
class F1Repository(private val api: ErgastApi) {

    suspend fun getCalendar(): List<Race> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val response = api.getRaces(currentYear)

        return response.mrData.raceTable.races.map { raceData ->
            val rawTimeString = raceData.time ?: "00:00:00Z"
            val dateTimeString = "${raceData.date}T$rawTimeString"

            val raceInstant = try {
                ZonedDateTime.parse(dateTimeString).toInstant()
            } catch (e: Exception) {
                e.printStackTrace()
                Instant.now()
            }

            Race(
                round = raceData.round,
                raceName = raceData.raceName,
                circuit = raceData.circuit,
                date = raceData.date,
                time = raceData.time,
                firstPractice = raceData.firstPractice,
                secondPractice = raceData.secondPractice,
                thirdPractice = raceData.thirdPractice,
                qualifying = raceData.qualifying,
                sprint = raceData.sprint,
                sprintQualifying = raceData.sprintQualifying,
                raceInstant = raceInstant
            )
        }
    }

    // Puan Durumunu Çeken Fonksiyon
    suspend fun getDriverStandings(): List<DriverStanding> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val response = api.getDriverStandings(currentYear)
        return response.mrData.standingsTable.standingsLists.firstOrNull()?.driverStandings ?: emptyList()
    }

    // YENİ: Yarış Sonuçlarını Çeken Fonksiyon
    suspend fun getRaceResults(round: String): List<BaseResult> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        val response = api.getRaceResults(currentYear, round)
        return response.mrData.raceTable.races.firstOrNull()?.results ?: emptyList()
    }

    // YENİ: Sıralama Sonuçlarını Çeken Fonksiyon
    suspend fun getQualifyingResults(round: String): List<BaseResult> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        val response = api.getQualifyingResults(currentYear, round)
        return response.mrData.raceTable.races.firstOrNull()?.results ?: emptyList()
    }

    // YENİ: Sprint Sonuçlarını Çeken Fonksiyon
    suspend fun getSprintResults(round: String): List<BaseResult> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        val response = api.getSprintResults(currentYear, round)
        return response.mrData.raceTable.races.firstOrNull()?.results ?: emptyList()
    }
    // Sprint Qualifying Sonuç fonksiyonu (Şimdilik boş)
    suspend fun getSprintQualifyingResults(year: Int, round: String): List<BaseResult> {
        return emptyList()
    }
}