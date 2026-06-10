plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Thêm plugin Gradle của dịch vụ Google
    id ( "com.google.gms.google-services" )

}

android {
    // Default cho máy thật qua USB + adb reverse.
    val mobileApiHost = (project.findProperty("MOBILE_API_HOST") as String?) ?: "127.0.0.1"
    val mobileApiPort = (project.findProperty("MOBILE_API_PORT") as String?) ?: "5000"
    val shareWebBaseUrl = (project.findProperty("SHARE_WEB_BASE_URL") as String?) ?: "https://sport-management.vn"
    val paymentSandboxAutoComplete =
        project.findProperty("PAYMENT_SANDBOX_AUTO_COMPLETE") as String?

    namespace = "com.sportmanagement.user"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sportmanagement.user"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_BASE_URL", "\"http://$mobileApiHost:$mobileApiPort\"")
        buildConfigField("String", "DEEP_LINK_SCHEME", "\"sportmanagement\"")
        buildConfigField("String", "SHARE_WEB_BASE_URL", "\"$shareWebBaseUrl\"")
        buildConfigField("String", "DEEP_LINK_WEB_BASE_URL", "\"$shareWebBaseUrl\"")
        buildConfigField(
            "String",
            "APP_DOWNLOAD_URL",
            "\"https://play.google.com/store/apps/details?id=com.sportmanagement.user\""
        )
        manifestPlaceholders["deepLinkHost"] = mobileApiHost

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = false
        }
    }

    buildTypes {
        debug {
        buildConfigField(
                "boolean",
                "PAYMENT_SANDBOX_AUTO_COMPLETE",
                paymentSandboxAutoComplete ?: "true"
            )
        }
        release {
            buildConfigField("boolean", "PAYMENT_SANDBOX_AUTO_COMPLETE", "false")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Maps & Location
    implementation("com.google.android.gms:play-services-maps:20.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.compose.material3:material3:1.4.0")

    // Compose BOM (Quản lý phiên bản tập trung)
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core & Lifecycle
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    // UI Libraries (Không ghi phiên bản để lấy từ BOM)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Third-party Libraries
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.google.android.material:material:1.12.0")
    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation("org.maplibre.gl:android-sdk:11.0.0")
    implementation("com.google.zxing:core:3.5.3")

    // Debug & Test
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    implementation("com.google.android.gms:play-services-auth:21.2.0")
}

