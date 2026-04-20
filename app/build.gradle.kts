plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.trailtrack"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.trailtrack"
        minSdk = 29
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.glide)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.firebase.messaging)
    implementation(libs.material)
    implementation("org.osmdroid:osmdroid-android:6.1.16") {
        exclude(group = "com.j256.ormlite", module = "ormlite-core")
    }
    implementation("org.osmdroid:osmdroid-wms:6.1.16") {
        exclude(group = "com.j256.ormlite", module = "ormlite-core")
    }
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.16") {
        exclude(group = "com.j256.ormlite", module = "ormlite-core")
    }
    implementation("org.osmdroid:osmdroid-geopackage:6.1.16") {
        exclude(group = "com.j256.ormlite", module = "ormlite-core")
    }
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}