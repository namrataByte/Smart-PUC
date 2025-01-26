plugins {
    alias(libs.plugins.android.application) apply false

    id("com.google.gms.google-services") version "4.4.2" apply false
}


buildscript {
    repositories {
        google()  // Make sure this line is present
        mavenCentral()
        maven("https://jitpack.io") // Add JitPack here as well
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:8.5.2")
        classpath ("com.google.gms:google-services:4.3.15")
        classpath("com.github.PhilJay:MPAndroidChart:v3.1.0") // Ensure this line is present
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // Add JitPack here as well
    }
}