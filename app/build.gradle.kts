plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.foodapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.foodapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// Dependencies from libs.versions.toml
val composeBom by extra { libs.findVersion("compose-bom").get() }
val composeMaterial3 by extra { libs.findVersion("compose-material3").get() }
val composeNavigation by extra { libs.findVersion("compose-navigation").get() }
val composeLifecycleViewmodel by extra { libs.findVersion("compose-lifecycle-viewmodel").get() }
val coroutinesAndroid by extra { libs.findVersion("kotlinx-coroutines").get() }
val supabaseGotrue by extra { libs.findVersion("gotrue-kt").get() }
val supabasePostgrest by extra { libs.findVersion("postgrest-kt").get() }
val facebookLogin by extra { libs.findVersion("facebook-login").get() }
val ktorClient by extra { libs.findVersion("ktor-client").get() }
val credentialManager by extra { libs.findVersion("credential-manager").get() }

// Don't forget to include firebase-auth if needed

val kotlinVersion = "1.9.22"

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Compose with BOM
    implementation(platform("androidx.compose:compose-bom:${composeBom.version}"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3:${composeMaterial3.version}")
    implementation("androidx.navigation:navigation-compose:${composeNavigation.version}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${composeLifecycleViewmodel.version}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${composeLifecycleViewmodel.version}")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${coroutinesAndroid.version}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesAndroid.version}")

    // Supabase
    implementation("io.github.jan-tennert.supabase:supabase-gotrue-kt:${supabaseGotrue.version}")
    implementation("io.github.jan-tennert.supabase:supabase-postgrest-kt:${supabasePostgrest.version}")

    // Facebook Login
    implementation("com.facebook.android:facebook-login:${facebookLogin.version}")

    // Ktor for HTTP requests
    implementation("io.ktor:ktor-client-core:${ktorClient.version}")
    implementation("io.ktor:ktor-client-cio:${ktorClient.version}")

    // Credential Manager
    implementation("androidx.credentials:credentials:${credentialManager.version}")
    implementation("androidx.credentials:credentials-gms:${credentialManager.version}")

    // Additional Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
}
