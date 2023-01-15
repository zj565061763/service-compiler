val kspVersion: String by project

plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
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
                version = "1.0.0-alpha02"
            }
        }
    }
}

