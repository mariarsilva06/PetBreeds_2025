import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// API keys - local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.petbreeds.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        buildConfigField(
            "String",
            "CAT_API_KEY",
            "\"${localProperties.getProperty("CAT_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "DOG_API_KEY",
            "\"${localProperties.getProperty("DOG_API_KEY", "")}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    api(project(":core:common"))
    api(project(":core:model"))

    api(libs.retrofit)
    api(libs.retrofit.gson)

    implementation(libs.okhttp.logging)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.coroutines.android)
}