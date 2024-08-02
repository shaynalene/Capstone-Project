import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("plugin.serialization") version "2.0.0"
}

android {
    namespace = "com.example.voiceassistant"
    compileSdk = 34

    packagingOptions {
        exclude ("META-INF/DEPENDENCIES")
        exclude ("META-INF/NOTICE.md")
        exclude ("META-INF/LICENSE.md")
    }


    defaultConfig {
        applicationId = "com.example.voiceassistant"
        minSdk = 21
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

}





dependencies {
    // Use the BOM to manage Supabase versions
    implementation(platform("io.github.jan-tennert.supabase:bom:2.5.4"))


    // Supabase libraries (do not specify versions, managed by the BOM)
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")

    // Ktor libraries
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-utils:2.3.12")

    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // Android dependencies
    //implementation("com.google.android.material:material:1.6.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.firebase.dataconnect)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    /*qr scanner start*/
    implementation ("com.github.yuriy-budiyev:code-scanner:2.3.0")
    /*qr scanner end*/

    /*qr generator start*/
    implementation ("com.google.zxing:core:3.4.1")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    /*qr generator end*/

    // OkHttp for HTTP requests
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    //implementation("com.sendgrid:sendgrid-java:4.10.1")

    implementation("com.sun.mail:android-mail:1.6.6")
    implementation("com.sun.mail:android-activation:1.6.6")

    // Additional libraries
    implementation("org.apache.commons:commons-csv:1.8")
    implementation("org.apache.opennlp:opennlp-tools:1.9.3")

    //implementation ("io.github.jan.supabase:supabase-client:0.2.0") // Replace with the correct version
    //implementation ("io.github.jan.supabase:postgrest:0.1.0") // Replace with the correct version
    implementation ("com.github.kittinunf.fuel:fuel:2.3.1")

    // Design using Materials
    implementation ("com.google.android.material:material:1.6.0")

}