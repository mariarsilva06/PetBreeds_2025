plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.example.resources"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
}
