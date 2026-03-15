import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

configure<ApplicationExtension> {
    compileSdk = 36
    defaultConfig {
        applicationId = "eu.andret.kalendarzswiatnietypowych"
        minSdk = 29
        targetSdk = 36
        versionCode = 96
        versionName = "3.0.0-build.20"
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
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
        debug {
            isShrinkResources = false
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
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
        includeInApk = false
        includeInBundle = false
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
    implementation(libs.picasso)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.emoji.java)

    annotationProcessor(libs.room.compiler)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
