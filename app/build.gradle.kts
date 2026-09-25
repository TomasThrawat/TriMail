plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.trimaill.app"
    compileSdk = 35

    val googleWebClientId = providers.gradleProperty("GOOGLE_WEB_CLIENT_ID").orNull.orEmpty()
        .replace("\", "\\")
        .replace(""", "\"")
        .replace(""", "\"")

    defaultConfig {
        applicationId = "com.trimaill.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", ""$escapedGoogleWebClientId"")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.credentials:credentials:1.7.0-alpha02")
    implementation("androidx.credentials:credentials-play-services-auth:1.7.0-alpha02")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

    testImplementation("junit:junit:4.13.2")
}
