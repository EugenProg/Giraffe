plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.compose)
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
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.grpc.stub)
    implementation(libs.kogen.di)
    ksp(libs.kogen.di)
    implementation(libs.androidx.navigation)
    implementation(libs.koGenNavigation)
    ksp(libs.koGenNavigation)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.coroutines)

    // Compose
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.coil.base)
    implementation(libs.coil)
}

ksp {
    arg("packageName", "com.kogen.giraffe")
    arg("includeViewModelInjector", "true")
    arg("defaultAnimation", "slideLeft")
    arg("room.schemaLocation", "$projectDir/schemas")
}
