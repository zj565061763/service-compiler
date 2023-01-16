plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":service"))
    ksp(project(":compiler"))
}

kotlin {
    jvmToolchain(11)
}

ksp {
    arg("FSERVICE_MODULE_NAME", project.name)
}
