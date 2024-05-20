plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.example.Capstone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.Capstone"
        minSdk = 24
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
    buildFeatures {
        viewBinding = true
    }
}
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    /////////////////서버통신 라이브러리/////////////////////
    implementation("com.squareup.retrofit2:retrofit:2.6.1")
    implementation("com.squareup.retrofit2:converter-gson:2.6.1")
    implementation ("com.squareup.okhttp3:logging-interceptor:3.12.1")
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")

    ////////////////GPS 라이브러리//////////////////////////
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.github.bumptech.glide:glide:4.14.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.1")
    implementation("com.soundcloud.android:android-crop:1.0.1@aar")
}