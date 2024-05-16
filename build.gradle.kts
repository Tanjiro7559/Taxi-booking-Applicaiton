buildscript {
    dependencies {
        classpath(libs.google.services)

            classpath ("com.android.tools.build:gradle:3.4.1")
            classpath ("com.google.gms:google-services:4.2.0")
        classpath ("com.google.gms:google-services:4.4.1")


    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin) apply false

}