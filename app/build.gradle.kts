plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
//    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.signalapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.signalapp"
        minSdk = 31
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material)

    // Firebase BOM (保持最新)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.room.common)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Material Icons Core (通常已經有了)
    implementation(libs.androidx.material.icons.core) // 確保版本與其他 Compose 庫一致
    implementation(libs.androidx.material.icons.extended) // 使用與其他 Compose 庫相同的版本

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose) // 檢查最新版本
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // ViewModelScope 需要

    // Also add the dependency for the Google Play services library and specify its version
    implementation(libs.play.services.auth)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.android)

    //room
    implementation(libs.androidx.room.runtime)
    // 使用 KSP (推薦)
    ksp(libs.androidx.room.compiler)
    // 可選 - Kotlin 擴展和對 Coroutines 的支持 (幾乎必選)
    implementation(libs.androidx.room.ktx)
    // 可選 - Testing Room
    // testImplementation("androidx.room:room-testing:$room_version")
    // androidTestImplementation("androidx.room:room-testing:$room_version")

    // Hilt
    implementation(libs.hilt.android.v2511)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose.v110)

    implementation(libs.jbcrypt)
    implementation(libs.coil.compose)
}

