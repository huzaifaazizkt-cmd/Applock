plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.applock"

    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.applock"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))

    // Core Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Material3 (Compose UI ke liye)
    implementation("androidx.compose.material3:material3")

    // Activity
    implementation("androidx.activity:activity-compose:1.9.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Icons
    implementation("androidx.compose.material:material-icons-extended")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Material Components (XML theme ke liye REQUIRED)
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation(libs.androidx.compose.foundation)
    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
}