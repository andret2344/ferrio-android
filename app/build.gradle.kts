plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "eu.andret.kalendarzswiatnietypowych"
        minSdk = 23
        targetSdk = 35
        versionCode = 79
        versionName = "3.0.0-build.4"
        multiDexEnabled = true
        signingConfig = signingConfigs.getByName("debug")
    }
    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            isDebuggable = true
            isShrinkResources = false
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    namespace = "eu.andret.kalendarzswiatnietypowych"
    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    implementation(libs.play.services.ads)
    implementation(libs.material)
    implementation(libs.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.gson)
    implementation(libs.stream.support)
    implementation(libs.rhino)
    implementation(libs.picasso)
    implementation(libs.guava)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room)

    annotationProcessor(libs.room.compiler)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
