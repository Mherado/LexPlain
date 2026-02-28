import org.gradle.kotlin.dsl.testImplementation

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "mher.minasyanlexplain"
    compileSdk = 34

    @Suppress("UnstableApiUsage")
    androidResources {
        // Запрещаем сжатие для моделей и конфигов, чтобы ONNX и GSON читали их быстрее
        noCompress += listOf("onnx", "json", "txt")
    }

    defaultConfig {
        applicationId = "mher.minasyanlexplain"
        minSdk = 24
        targetSdk = 34
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


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}