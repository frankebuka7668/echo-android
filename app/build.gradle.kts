plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.echo.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.echo.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        val groqKey = (findProperty("GROQ_API_KEY") as String?)
            ?: System.getenv("GROQ_API_KEY")
            ?: ""
        buildConfigField("String", "GROQ_API_KEY", "\"${groqKey.replace("\\\\", "\\\\\\\\").replace("\"", "\\\\\"")}\"")
        buildConfigField("String", "GROQ_MODEL", "\"llama-3.1-70b-versatile\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            isMinifyEnabled = false
        }
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
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
