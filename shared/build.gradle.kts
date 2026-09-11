plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.ksp)
}

detekt {
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

kotlin {
    jvmToolchain(21)

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
            implementation(libs.serialization.json)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.libsignal.android)
            implementation(libs.room.runtime)
            implementation(libs.room.ktx)
            implementation(libs.sqlcipher.android)
            implementation(libs.sqlite.ktx)
            implementation(libs.security.crypto)
            implementation(libs.androidx.core)
        }
        jvmMain.dependencies {
            implementation(libs.libsignal.client)
        }
    }
}

android {
    namespace = "com.veil.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    dependencies {
        add("kspAndroid", libs.room.compiler)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
