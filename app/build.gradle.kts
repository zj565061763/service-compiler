plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":app_lib"))
    implementation(project(":app_module_a"))
    implementation(project(":app_module_b"))
    ksp(project(":lib"))
}

ksp {
    arg("FSERVICE_MODULE_NAME", "FSERVICE_MODULE_MAIN")
}