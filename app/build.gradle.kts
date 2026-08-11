import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ktlint)
    kotlin("kapt")
}
ktlint {
    filter {
        exclude("**/androidTest/**")
        exclude("**/test/**")
    }
}

val localProperties = Properties().apply {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) {
        load(propsFile.inputStream())
    }
}

android {
    namespace = "com.tbasic.fitnesstracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tbasic.fitnesstracker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "DATABASE_URL",
            "\"${localProperties["DATABASE_URL"]}\""
        )
        buildConfigField (
            "String",
            "API_KEY",
            "${localProperties["API_KEY"]}"
        )
        buildConfigField (
            "String",
            "API_URL",
            "${localProperties["API_URL"]}"
        )
        buildConfigField (
            "String",
            "API_MODEL_ID",
            "${localProperties["API_MODEL_ID"]}"
        )
        buildConfigField (
            "String",
            "PHOTON_URL",
            "${localProperties["PHOTON_URL"]}"
        )
        buildConfigField (
            "String",
            "PHOTON_USER_AGENT",
            "${localProperties["PHOTON_USER_AGENT"]}"
        )
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
        buildConfig = true
    }
}

dependencies {
    ksp(libs.room.compiler.ksp)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.mlkit.translate)
    implementation(libs.coil.compose)
    implementation(libs.coil.kt.coil.gif)
    implementation(libs.lottie.compose)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.openai.client)

    // Ktor engine za HTTP zahtjeve
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.cio)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.runtime.livedata)

    implementation(libs.retrofit)
    implementation(libs.gson.converter)
    implementation(libs.gson.core)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(platform(libs.firebase.bom))
// Firebase Realtime Database
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.gotrue)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.work.hilt)
    implementation(libs.androidx.hilt.work.v100)
    ksp(libs.androidx.hilt.compiler.v110)
    ksp(libs.hilt.compiler.v2481)
    implementation(libs.accompanist.placeholder.material)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
