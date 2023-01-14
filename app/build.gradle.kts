plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":app_lib"))
    implementation(project(":app_module_a"))
    implementation(project(":app_module_b"))
    ksp(project(":lib"))
}

kotlin {
    jvmToolchain(8)
}

ksp {
    arg("FSERVICE_MODULE_NAME", "FSERVICE_MODULE_MAIN")
}