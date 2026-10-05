plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}

// Single source of truth: the VERSION file at the repo root. CI releases by reading
// the same file, so the APK, the in-app indicator, and the git tag can never drift.
// ponytail: versionCode is derived rather than hand-maintained; it only needs to
// increase monotonically for Android, which this guarantees for any sane semver.
val appVersion: String = file("$rootDir/VERSION").readText().trim()
val appVersionCode: Int = appVersion.split(".")
    .let { (major, minor, patch) -> major.toInt() * 100_000 + minor.toInt() * 100 + patch.toInt() }

android {
    namespace = "com.nnoidea.fitnez2"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.nnoidea.fitnez2"
        minSdk = 31
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.maxHeapSize = "2048m"
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.gson)

    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

detekt {
    // Ratchet: only new/changed lines fail. Existing debt stays visible in reports
    // without blocking work. Delete the baseline once the backlog is cleared.
    baseline = file("detekt-baseline.xml")
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt.yml"))
    source.setFrom("src/main/java", "src/test/java")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*_Factory*",
                    "*ComposableSingletons*",
                    "*_Impl*",
                    "*_Impl$*",
                    "*.BuildConfig",
                    "*Preview*"
                )
                packages(
                    "com.nnoidea.fitnez2.ui.theme"
                )
            }
        }
        total {
            html {
                onCheck = false
            }
            xml {
                onCheck = false
            }
            log {
                onCheck = false
            }
        }
    }
}

