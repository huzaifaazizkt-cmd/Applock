plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.applock"

    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.applock"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // Compose BOM
    implementation(
        platform(
            "androidx.compose:compose-bom:2026.02.01"
        )
    )

    // Compose UI
    implementation(
        "androidx.compose.ui:ui"
    )

    implementation(
        "androidx.compose.ui:ui-tooling-preview"
    )

    implementation(
        "androidx.compose.ui:ui-graphics"
    )

    // Foundation
    implementation(
        "androidx.compose.foundation:foundation"
    )

    // Material 3
    implementation(
        "androidx.compose.material3:material3"
    )

    // Material Icons
    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    // Activity
    implementation(
        "androidx.activity:activity-compose:1.13.0"
    )

    // Navigation
    implementation(
        "androidx.navigation:navigation-compose:2.9.8"
    )

    // DataStore
    implementation(
        "androidx.datastore:datastore-preferences:1.1.7"
    )

    // Lifecycle
    implementation(
        "androidx.lifecycle:lifecycle-runtime-ktx:2.11.0"
    )

    // Material Components
    implementation(
        "com.google.android.material:material:1.13.0"
    )

    // Biometric
    implementation(
        "androidx.biometric:biometric:1.1.0"
    )

    // Coil
    implementation(
        "io.coil-kt:coil-compose:2.7.0"
    )
    implementation("androidx.camera:camera-camera2:1.5.0")
    implementation("androidx.camera:camera-lifecycle:1.5.0")
    implementation("androidx.camera:camera-core:1.5.0")
    // Debug tooling
    debugImplementation(
        "androidx.compose.ui:ui-tooling"
    )
}