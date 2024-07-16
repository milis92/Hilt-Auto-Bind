plugins {
    `kgc-root`
}

allprojects {
    group = "com.github.milis92.hiltautobind"
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.register("versionToFile") {
    doLast {
        file("version.txt").writeText(version.toString())
    }
}
