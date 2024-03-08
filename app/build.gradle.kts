plugins {
    id("com.android.application")
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "eu.andret.kalendarzswiatnietypowych"
        minSdk = 26
        targetSdk = 34
        versionCode = 69
        versionName = "2.4.0-rc.1"
        multiDexEnabled = true
        signingConfig = signingConfigs.getByName("debug")
    }
    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
            isShrinkResources = false
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
    implementation(group = "com.google.android.gms", name = "play-services-ads", version = "22.6.0")
    implementation(group = "com.google.android.material", name = "material", version = "1.12.0-alpha03")

    implementation(group = "com.android.support", name = "multidex", version = "2.0.1")

    implementation(group = "com.google.code.gson", name = "gson", version = "2.10.1")
    implementation(group = "net.sourceforge.streamsupport", name = "android-retrofuture", version = "1.7.4")
    implementation(group = "org.mozilla", name = "rhino", version = "1.7.14")

    implementation(group = "androidx.appcompat", name = "appcompat", version = "1.6.1")
    implementation(group = "androidx.preference", name = "preference", version = "1.2.1")
    implementation(group = "androidx.viewpager2", name = "viewpager2", version = "1.0.0")
    implementation(group = "androidx.navigation", name = "navigation-fragment", version = "2.7.7")
    implementation(group = "androidx.navigation", name = "navigation-ui", version = "2.7.7")
    implementation(group = "androidx.cardview", name = "cardview", version = "1.0.0")
    implementation(group = "androidx.room", name = "room-runtime", version = "2.6.1")

    annotationProcessor(group = "androidx.room", name = "room-compiler", version = "2.6.1")
    annotationProcessor(group = "org.jetbrains", name = "annotations", version = "24.1.0")
    coreLibraryDesugaring(group = "com.android.tools", name = "desugar_jdk_libs", version = "2.0.4")
}
