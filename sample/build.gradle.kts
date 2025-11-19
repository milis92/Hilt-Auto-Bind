plugins {
    id("com.android.application")
    kotlin("android")
    `kgc-kotlin`
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.herman.hiltautobind.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.herman.hiltautobind.sample"
        minSdk = 24

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.herman.hiltautobind.sample.demo.HiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    compileOnly(libs.hilt.core)
    implementation(libs.hilt.android)
    implementation(projects.runtime)

    ksp(projects.processor)
    ksp(libs.hilt.processor)
    kspTest(projects.processor)
    kspTest(libs.hilt.processor)

    testImplementation(libs.bundles.androidx.test)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.kotlin.junit)
}
