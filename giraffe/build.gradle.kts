plugins {
    id("com.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kogen.giraffe"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    sourceSets {
        getByName("debug") {
            java.directories.add("build/generated/ksp/debug/kotlin")
        }
        getByName("release") {
            java.directories.add("build/generated/ksp/release/kotlin")
        }
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.grpc.stub)
    implementation(libs.kogen.di)
    ksp(libs.kogen.di)
}

ksp {
    arg("packageName", "com.kogen.giraffe")
}
