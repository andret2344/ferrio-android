plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

repositories {
    google()
    mavenCentral()
}

android {
    namespace = "eu.andret.kalendarzswiatnietypowych"
    compileSdk = 37
    defaultConfig {
        applicationId = "eu.andret.kalendarzswiatnietypowych"
        minSdk = 24
        targetSdk = 37
        versionCode = 101
        versionName = "3.1.2"
    }
    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.room)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.viewpager2)
    implementation(libs.emoji.java)
    implementation(libs.firebase.auth)
    implementation(libs.glide)
    implementation(libs.googleid)
    implementation(libs.gson)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.play.integrity)
    implementation(libs.play.services.ads)
    implementation(libs.play.services.auth)

    annotationProcessor(libs.room.compiler)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.assertj)
    testImplementation(libs.junit)
}
