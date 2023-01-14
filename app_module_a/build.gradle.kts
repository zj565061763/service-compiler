plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":app_lib"))
    ksp(project(":lib"))
}

kotlin {
    jvmToolchain(8)
}

ksp {
    arg("FSERVICE_MODULE_NAME", project.name)
}