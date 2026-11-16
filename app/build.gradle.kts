import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.htmlbackgroundrunner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.htmlbackgroundrunner"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"
    }

    // Android Gradle Plugin uses these settings for Java compilation.
    // Do not configure JavaCompile.options.release in an Android module.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
