plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    buildToolsVersion = "35.0.0"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dessalines.rankmyfavs"
        minSdk = 21
        targetSdk = 35
        versionCode = 31
        versionName = "0.6.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ksp { arg("room.schemaLocation", "$projectDir/schemas") }
    }

    // Necessary for izzyondroid releases
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    if (project.hasProperty("RELEASE_STORE_FILE")) {
        signingConfigs {
            create("release") {
                storeFile = file(project.property("RELEASE_STORE_FILE")!!)
                storePassword = project.property("RELEASE_STORE_PASSWORD") as String?
                keyAlias = project.property("RELEASE_KEY_ALIAS") as String?
                keyPassword = project.property("RELEASE_KEY_PASSWORD") as String?

                // Optional, specify signing versions used
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }
    buildTypes {
        release {
            if (project.hasProperty("RELEASE_STORE_FILE")) {
                signingConfig = signingConfigs.getByName("release")
            }

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                // Includes the default ProGuard rules files that are packaged with
                // the Android Gradle plugin. To learn more, go to the section about
                // R8 configuration files.
                getDefaultProguardFile("proguard-android-optimize.txt"),

                // Includes a local, custom Proguard rules file
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = " (DEBUG)"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjvm-default=all-compatibility", "-opt-in=kotlin.RequiresOptIn")
    }
    buildFeatures {
        compose = true
    }
    namespace = "com.dessalines.rankmyfavs"
}

dependencies {
    // Color picker
    implementation("com.github.skydoves:colorpicker-compose:1.1.2")

    // Exporting / importing DB helper
    implementation("com.github.dessalines:room-db-export-import:0.1.0")

    // Composable screenshot
    implementation("dev.shreyaspatil:capturable:3.0.1")

    // CSV exporting
    implementation("com.floern.castingcsv:casting-csv-kt:1.2")

    // Tables
    implementation("com.github.Breens-Mbaka:BeeTablesCompose:1.2.0")

    // Glicko2
    implementation("com.github.goochjs:glicko2:master")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2025.01.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.7")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.7")

    // Adaptive layouts
    implementation("androidx.compose.material3.adaptive:adaptive:1.1.0-beta01")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.1.0-beta01")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.1.0-beta01")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite")

    // Activities
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.activity:activity-ktx:1.10.0")

    // LiveData
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.6")

    // Markdown
    implementation("com.github.jeziellago:compose-markdown:0.5.6")

    // Preferences
    implementation("me.zhanghai.compose.preference:library:1.1.1")

    // Room
    // To use Kotlin annotation processing tool
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.6.1")

    // App compat
    implementation("androidx.appcompat:appcompat:1.7.0")
}
