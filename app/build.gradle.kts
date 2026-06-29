plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kover)
}

// Version: the git tag (vMAJOR.MINOR.PATCH) is the single source of truth; the release
// workflow passes it in as VERSION_NAME. Local/debug builds fall back to a dev version.
val appVersionName: String =
    (System.getenv("VERSION_NAME")?.takeIf { it.isNotBlank() } ?: "0.1.0").removePrefix("v")
val semver: List<String> = appVersionName.split(".")
val appVersionCode: Int = (semver.getOrNull(0)?.toIntOrNull() ?: 0) * 1_000_000 +
    (semver.getOrNull(1)?.toIntOrNull() ?: 0) * 1_000 +
    (semver.getOrNull(2)?.toIntOrNull() ?: 0)

// Optional release signing — supplied via env in CI; absent locally so debug still builds.
val ksFile = System.getenv("KEYSTORE_PATH")?.takeIf { it.isNotBlank() }
    ?.let { rootProject.file(it).absoluteFile }?.takeIf { it.isFile }
val ksPassword = System.getenv("KEYSTORE_PASSWORD")?.takeIf { it.isNotBlank() }
val ksAlias = System.getenv("KEY_ALIAS")?.takeIf { it.isNotBlank() }
val ksKeyPassword = System.getenv("KEY_PASSWORD")?.takeIf { it.isNotBlank() }
val hasSigning = ksFile != null && ksPassword != null && ksAlias != null && ksKeyPassword != null

android {
    namespace = "dk.cocode.chess"
    compileSdk = 35

    defaultConfig {
        applicationId = "dk.cocode.chess"
        minSdk = 24
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
    }
    signingConfigs {
        if (hasSigning) {
            create("release") {
                storeFile = ksFile
                storePassword = ksPassword
                keyAlias = ksAlias
                keyPassword = ksKeyPassword
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasSigning) signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
}

// Coverage and CI only target the debug variant; disable release unit tests so Kover does not run
// them (they lack the debug-only Compose `ui-test-manifest` needed by the Robolectric UI tests).
tasks.withType(org.gradle.api.tasks.testing.Test::class.java).configureEach {
    if (name.contains("Release")) enabled = false
}
