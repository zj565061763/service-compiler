plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":app_service"))
    implementation(project(":app_feature"))
    ksp(project(":lib"))
}

kotlin {
    jvmToolchain(11)
}

ksp {
    arg("FSERVICE_MODULE_NAME", "FSERVICE_MODULE_MAIN")
}