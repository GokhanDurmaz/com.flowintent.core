plugins {
    alias(libs.plugins.androidLibrary) // Android Library eklentisi
    alias(libs.plugins.jetbrainsKotlinAndroid) // Kotlin Android eklentisi
    id("maven-publish")
}

android {
    namespace = "com.flowintent.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.flowintent" // Grup ID
                artifactId = "flowintent-core" // Artifact ID
                version = "1.0.1" // Version
            }
        }
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/GokhanDurmaz/com.flowintent.core")
                credentials {
                    username = System.getenv("GITHUB_USERNAME") ?: ""
                    password = System.getenv("GITHUB_TOKEN") ?: ""
                }
            }
        }
    }
}