plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.f1calendarwatchapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.f1calendarwatchapp"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    // ----------------------------------------------------
    // Play Services ve Compose BOM
    // ----------------------------------------------------
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))

    // ----------------------------------------------------
    // Core Compose Kütüphaneleri
    // Standart Foundation ve Material yerine, Wear Foundation ve Material kullanacağız.
    // Bu yüzden libs.compose.foundation/material satırları ÇIKARILDI.
    // ----------------------------------------------------
    implementation(libs.ui) // Standart Compose UI (Gereklidir)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)

    // DOĞRU VE KRİTİK EKLENTİLER: ScalingLazyColumn için bunlar ZORUNLU
    implementation("androidx.wear.compose:compose-material:1.3.1")
    implementation("androidx.wear.compose:compose-foundation:1.3.1")
    implementation("androidx.wear.compose:compose-navigation:1.5.5") // Navigasyon için zorunlu

    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)

    // ----------------------------------------------------
    // Test ve Debug Dependencies
    // ----------------------------------------------------
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // ----------------------------------------------------
    // Utility & Arka Plan Kütüphaneleri
    // ----------------------------------------------------
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.4")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Coroutines (Asenkron İşlemler)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Retrofit (API İstemcisi)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ViewModel ve Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

    // Icons.Filled ikonları için
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.11.0")

    implementation("androidx.compose.material:material:1.6.0")
}