plugins {
    alias(libs.plugins.android.application)
    // WAJIB: Plugin Google Services untuk Firebase
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.crudprodukapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.crudprodukapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}
dependencies {
    // Dependensi Standar
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Firebase BOM (Bill of Materials) - WAJIB
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Firebase Firestore (Database Utama)
    implementation("com.google.firebase:firebase-firestore")

    // Firebase Realtime Database (Sesuai referensi awal dan permintaan)
    implementation("com.google.firebase:firebase-database")

    // Firebase Analytics (opsional)
    implementation("com.google.firebase:firebase-analytics")
    // --- END FIREBASE DEPENDENCIES ---

    // UI & Utilitas
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.11.0")

    // TESTING
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}