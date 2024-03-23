plugins {
    id("com.android.application")
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "eu.andret.kalendarzswiatnietypowych"
        minSdk = 26
        targetSdk = 34
        versionCode = 71
        versionName = "3.0.0-rc.1"
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
val versionMultidex = "2.0.1"
val versionGson = "2.10.1"
val versionAndroidRetroFuture = "1.7.4"
val versionMozillaRhino = "1.7.14"
val versionAppCompat = "1.6.1"
val versionAndroidxPreference = "1.2.1"
val versionAndroidxViewPager2 = "1.0.0"
val versionAndroidxCardView = "1.0.0"
val versionAndroidxRoom = "2.6.1"
val versionJetBrainsAnnotations = "24.1.0"
val versionJdkLibs = "2.0.4"

dependencies {
    implementation("com.google.android.gms:play-services-ads:${versionPlayAdsService}")
    implementation("com.android.support:multidex:${versionMultidex}")
    implementation("com.google.code.gson:gson:${versionGson}")
    implementation("net.sourceforge.streamsupport:android-retrofuture:${versionAndroidRetroFuture}")
    implementation("org.mozilla:rhino:${versionMozillaRhino}")
    implementation("androidx.appcompat:appcompat:${versionAppCompat}")
    implementation("androidx.preference:preference:${versionAndroidxPreference}")
    implementation("androidx.viewpager2:viewpager2:${versionAndroidxViewPager2}")
    implementation("androidx.cardview:cardview:${versionAndroidxCardView}")
    implementation("androidx.room:room-runtime:${versionAndroidxRoom}")
    annotationProcessor("androidx.room:room-compiler:${versionAndroidxRoom}")
    annotationProcessor("org.jetbrains:annotations:${versionJetBrainsAnnotations}")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${versionJdkLibs}")
}
