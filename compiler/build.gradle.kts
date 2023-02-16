plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation(libs.google.devtools.ksp.api)
    implementation(libs.squareup.kotlinpoet.ksp)
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

kotlin {
    jvmToolchain(11)
}

java {
    withSourcesJar()
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                groupId = "com.sd.lib.android"
                artifactId = "service-compiler"
                version = "1.0.0-alpha10"
            }
        }
    }
}

