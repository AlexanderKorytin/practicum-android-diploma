plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("ru.practicum.android.diploma.plugins.developproperties")
    id("kotlin-kapt")
}

android {
    namespace = "ru.practicum.android.diploma"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ru.practicum.android.diploma"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(type = "String", name = "HH_ACCESS_TOKEN", value = "\"${developProperties.hhAccessToken}\"")
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidX.core)
    implementation(libs.androidX.appCompat)

    // UI layer libraries
    implementation(libs.ui.material)
    implementation(libs.ui.constraintLayout)

    // region Unit tests
    testImplementation(libs.unitTests.junit)
    testImplementation(libs.unitTests.junit.jupiter)
    // endregion

    // region coroutines tests
    testImplementation(libs.coroutines.test)
    // endregion

    // region mockito framework
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    // endregion

    // region UI tests
    androidTestImplementation(libs.uiTests.junitExt)
    androidTestImplementation(libs.uiTests.espressoCore)
    // endregion

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // room
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room)

    // glide
    implementation(libs.glide)

    // jetpack-navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // fragments
    implementation(libs.fragment)

    // coroutines
    implementation(libs.coroutines)

    // koin
    implementation(libs.koin)
}
