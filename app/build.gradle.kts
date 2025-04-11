plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services) // Đảm bảo plugin này được khai báo đúng
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.google.android.material:material:1.11.0") // hoặc phiên bản mới nhất

    implementation(libs.glide)
    implementation("com.tbuonomo:dotsindicator:4.3")
    implementation("androidx.media3:media3-exoplayer:1.6.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.6.0")
    implementation("androidx.media3:media3-ui:1.6.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Firebase Services
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // UI Components
    implementation(libs.androidx.cardview)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.media3.exoplayer)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}