plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "mher.minasyan.lexplain"
    compileSdk = 36

    @Suppress("UnstableApiUsage")
    androidResources {
        noCompress += listOf("onnx", "json", "txt")
    }

    defaultConfig {
        applicationId = "mher.minasyan.lexplain"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.android.gms:play-services-tasks:18.1.0")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.activity)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation(libs.firebase.firestore)



    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")

}