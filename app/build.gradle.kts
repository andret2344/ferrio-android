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
        versionCode = 71
        versionName = "3.0-rc.1"
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

val versionFirebase by extra("33.0.0")
val versionPlayServices by extra("23.2.0")
val versionMaterial by extra("1.12.0")
val versionPlayServicesAuth by extra("21.2.0")
val versionGoogleId by extra("1.1.1")
val versionGson by extra("2.10.1")
val versionStreamSupport by extra("1.7.4")
val versionRhino by extra("1.7.14")
val versionPicasso by extra("2.8")
val versionGuava by extra("31.1-android")
val versionAndroidXAppcompat by extra("1.7.0")
val versionAndroidXCredentials by extra("1.2.2")
val versionAndroidXCredentialsPlayServicesAuth by extra("1.2.2")
val versionAndroidXPreference by extra("1.2.1")
val versionAndroidXViewPager2 by extra("1.1.0")
val versionAndroidXCardView by extra("1.0.0")
val versionAndroidXActivity by extra("1.9.0")
val versionAndroidXConstraintLayout by extra("2.1.4")
val versionAndroidxRoom by extra("2.6.1")
val versionDesguarJdkLibs by extra("2.0.4")

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:$versionFirebase"))
    implementation("com.google.firebase:firebase-auth")

    implementation("com.google.android.gms:play-services-ads:$versionPlayServices")
    implementation("com.google.android.material:material:$versionMaterial")
    implementation("com.google.android.gms:play-services-auth:$versionPlayServicesAuth")
    implementation("com.google.android.libraries.identity.googleid:googleid:$versionGoogleId")

    implementation("com.google.code.gson:gson:$versionGson")
    implementation("net.sourceforge.streamsupport:android-retrofuture:$versionStreamSupport")
    implementation("org.mozilla:rhino:$versionRhino")
    implementation("com.squareup.picasso:picasso:$versionPicasso")
    implementation("com.google.guava:guava:$versionGuava")

    implementation("androidx.appcompat:appcompat:$versionAndroidXAppcompat")
    implementation("androidx.credentials:credentials:$versionAndroidXCredentials")
    implementation("androidx.credentials:credentials-play-services-auth:$versionAndroidXCredentialsPlayServicesAuth")
    implementation("androidx.preference:preference:$versionAndroidXPreference")
    implementation("androidx.viewpager2:viewpager2:$versionAndroidXViewPager2")
    implementation("androidx.cardview:cardview:$versionAndroidXCardView")
    implementation("androidx.activity:activity:$versionAndroidXActivity")
    implementation("androidx.constraintlayout:constraintlayout:$versionAndroidXConstraintLayout")
    implementation("androidx.room:room-runtime:$versionAndroidxRoom")

    annotationProcessor("androidx.room:room-compiler:$versionAndroidxRoom")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:$versionDesguarJdkLibs")
}
