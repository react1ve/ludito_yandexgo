import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

// Method to load YANDEX_MAPS_API_KEY from local.properties
fun getMapkitApiKey(): String {
    val properties = Properties()
    project.file("../local.properties").inputStream().use {
        properties.load(it)
    }
    return properties.getProperty("YANDEX_MAPS_API_KEY", "")
}

// Method to load YANDEX_GEOCODER_API_KEY from local.properties
fun getGeocoderApiKey(): String {
    val properties = Properties()
    project.file("../local.properties").inputStream().use {
        properties.load(it)
    }
    return properties.getProperty("YANDEX_GEOCODER_API_KEY", "")
}

android {
    compileSdk = 35
    namespace = "com.reactive.premier"

    defaultConfig {
        minSdk = 24
        targetSdk = 35
        multiDexEnabled = true

        buildConfigField("String", "YANDEX_MAPS_API_KEY", "\"${getMapkitApiKey()}\"")
        buildConfigField("String", "YANDEX_GEOCODER_API_KEY", "\"${getGeocoderApiKey()}\"")
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.androidx.recyclerview)
    api(libs.androidx.fragment.ktx)
    api(libs.material)
    api(libs.androidx.activity)
    api(libs.androidx.constraintlayout)

    api(libs.maps.mobile)

    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)

    api(libs.androidx.lifecycle.runtime)
    api(libs.androidx.lifecycle.viewmodel.ktx)

    api(libs.firebase.core)
    api(libs.firebase.analytics)

    api(libs.koin.android)

    implementation(libs.coil)

    implementation(libs.multidex)

    implementation(libs.timber)

    api(libs.play.services.location)
    api(libs.places)
    api(libs.json)

    implementation(libs.peko)
}
