plugins {
    `kgc-jvm-module`
}

dependencies {
    implementation(projects.runtime)
    implementation(libs.kotlin.ksp.api)

    ksp(libs.google.autoService.core)
    implementation(libs.google.autoService.annotations)
    implementation(libs.bundles.kotlin.poet)
    implementation(libs.hilt.core)

    testImplementation(libs.compileTesting)
    testImplementation(libs.compileTesting.ksp)
    testImplementation(libs.hilt.processor)
}