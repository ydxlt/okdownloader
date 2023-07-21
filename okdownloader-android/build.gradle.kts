plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.vanniktech.maven.publish")
    id("kotlin-kapt")
}

android {
    namespace = "com.billbook.lib.downloader"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.android.test.junit.ext)
    androidTestImplementation(libs.android.test.espresso)
    implementation(libs.commons.codec)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    api(project(path = ":okdownloader"))
}