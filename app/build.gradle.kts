plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)
}

android {
    namespace = "com.example.cab"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cab"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }



    dependencies {

        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation(libs.firebase.auth)
        implementation(libs.firebase.database)
        implementation(libs.play.services.maps)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
        implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

        implementation("com.google.firebase:firebase-auth:16.0.5")
        implementation("com.google.firebase:firebase-auth")

        implementation("com.google.android.gms:play-services-auth:21.1.0")
        implementation("com.google.android.gms:play-services-maps:18.2.0")
        implementation("com.google.android.gms:play-services-location:21.2.0")


        //implementation ("com.firebase:geofire-android:3.2.0")
        implementation("com.firebase:geofire-android:2.1.3")
        implementation("com.google.firebase:firebase-database:20.0.3")

    }
}