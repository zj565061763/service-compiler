plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":app_lib"))
    ksp(project(":lib"))
}

ksp {
    arg("FSERVICE_MODULE_NAME", project.name)
}