plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
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

    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("androidx.compose:compose-bom:2026.02.01"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.activity:activity-compose:1.13.0")

    implementation("androidx.navigation:navigation-compose:2.9.8")

    implementation("androidx.datastore:datastore-preferences:1.1.7")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")

    implementation("com.google.android.material:material:1.13.0")

    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))

    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-crashlytics")


    implementation("com.google.android.gms:play-services-ads:24.6.0")

    implementation("androidx.biometric:biometric:1.1.0")

    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("com.airbnb.android:lottie-compose:6.6.7")

    val billing_version = "9.1.0"

    implementation(
        "com.android.billingclient:billing-ktx:$billing_version"
    )

    implementation("com.google.android.play:review:2.0.2")

    implementation("androidx.camera:camera-camera2:1.5.0")

    implementation("androidx.camera:camera-lifecycle:1.5.0")

    implementation("androidx.camera:camera-core:1.5.0")


    debugImplementation("androidx.compose.ui:ui-tooling")
}