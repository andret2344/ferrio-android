plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "eu.andret.kalendarzswiatnietypowych"
        minSdk = 26
        targetSdk = 34
        versionCode = 70
        versionName = "2.3.0-rc.10"
        multiDexEnabled = true
        signingConfig = signingConfigs.getByName("debug")
    }
    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

val versionPlayAdsService = "23.0.0"
val versionMaterial = "1.12.0-rc01"
val versionMultidex = "2.0.1"
val versionPlayServicesAuth = "21.1.0"
val versionGson = "2.10.1"
val versionAndroidRetroFuture = "1.7.4"
val versionMozillaRhino = "1.7.14"
val versionAppCompat = "1.6.1"
val versionCredentials = "1.2.2"
val versionGoogleId = "1.1.0"
val versionAndroidxPreference = "1.2.1"
val versionAndroidxViewPager2 = "1.0.0"
val versionAndroidxCardView = "1.0.0"
val versionAndroidxActivity = "1.8.2"
val versionAndroidxConstraint = "2.1.4"
val versionAndroidxRoom = "2.6.1"
val versionJetBrainsAnnotations = "24.1.0"
val versionJdkLibs = "2.0.4"

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-auth")

    implementation("com.google.android.gms:play-services-ads:${versionPlayAdsService}")
    implementation("com.google.android.material:material:${versionMaterial}")

    implementation("com.android.support:multidex:${versionMultidex}")
    implementation("com.google.android.gms:play-services-auth:${versionPlayServicesAuth}")

    implementation("com.google.code.gson:gson:${versionGson}")
    implementation("net.sourceforge.streamsupport:android-retrofuture:${versionAndroidRetroFuture}")
    implementation("org.mozilla:rhino:${versionMozillaRhino}")

    implementation("androidx.appcompat:appcompat:${versionAppCompat}")
    implementation("androidx.credentials:credentials:${versionCredentials}")
    implementation("androidx.credentials:credentials-play-services-auth:${versionCredentials}")
    implementation("com.google.android.libraries.identity.googleid:googleid:${versionGoogleId}")
    implementation("androidx.preference:preference:${versionAndroidxPreference}")
    implementation("androidx.viewpager2:viewpager2:${versionAndroidxViewPager2}")
    implementation("androidx.cardview:cardview:${versionAndroidxCardView}")
    implementation("androidx.activity:activity:${versionAndroidxActivity}")
    implementation("androidx.constraintlayout:constraintlayout:${versionAndroidxConstraint}")
    implementation("androidx.room:room-runtime:${versionAndroidxRoom}")
    annotationProcessor("androidx.room:room-compiler:${versionAndroidxRoom}")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${versionJdkLibs}")
}
