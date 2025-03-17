plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.services) apply false
}

buildscript {
    repositories {
        google()
        jcenter()
        maven {
            setUrl("https://maven.google.com/")
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${libs.versions.agp}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin}")
        classpath("com.google.gms:google-services:${libs.versions.googleServices}")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}