plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.androidx.core)
            implementation(libs.lifecycle.runtime)
            implementation(libs.activity.compose)
            implementation(project.dependencies.platform("androidx.compose:compose-bom:2024.10.01"))
            implementation("androidx.compose.ui:ui")
            implementation("androidx.compose.material3:material3")
            implementation("androidx.compose.ui:ui-tooling-preview")
            implementation("androidx.compose.material:material-icons-extended")
            implementation(libs.koin.core)
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
        }
    }
}

android {
    namespace = "com.veil.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.veil.messenger"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField("String", "RELAY_BASE_URL", "\"http://10.0.2.2:8080\"")
        buildConfigField("boolean", "USE_MOCK_SERVICES", "true")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    debugImplementation("androidx.compose.ui:ui-tooling")
}
