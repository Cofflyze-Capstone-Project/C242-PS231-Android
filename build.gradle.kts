// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
buildscript {
    repositories {
        google() // Pastikan repositori google sudah ditambahkan
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.1") // Pastikan versi ini sesuai dengan proyek Anda
        classpath("com.google.gms:google-services:4.3.15") // Menambahkan plugin Google Servicesclasspath "androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3"
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3")
    }
}