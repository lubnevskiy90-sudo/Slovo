plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val keystorePath: String? = System.getenv("KEYSTORE_PATH")
val hasKeystore = keystorePath != null && File(keystorePath).length() > 0

android {
    namespace = "com.example.slovodnya"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.slovodnya"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        if (hasKeystore) {
            create("release") {
                storeFile = File(keystorePath!!)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
                ?: signingConfigs.getByName("debug")
        }
    }

    lint {
        checkReleaseBuilds = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
