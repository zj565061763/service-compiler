plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":app_lib"))
    implementation(project(":lib"))
    ksp(project(":lib"))
}

ksp {
    arg("FSERVICE_MODULE_NAME", project.name)
}
