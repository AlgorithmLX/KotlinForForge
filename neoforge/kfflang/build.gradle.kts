plugins {
    alias(libs.plugins.kotlinJvm)
    `maven-publish`
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
java.withSourcesJar()

repositories {
    maven("https://maven.neoforged.net/releases")
    // use mojang libraries without NeoGradle
    maven("https://repo.minebench.de/")
}

dependencies {
    implementation(libs.fancymodloader)

    // Maven dependencies
    api(libs.kotlin.reflect)
    api(libs.kotlin.stdlib.asProvider())
    api(libs.kotlin.stdlib.jdk7)
    api(libs.kotlin.stdlib.jdk8)
    api(libs.kotlinx.coroutines.core.asProvider())
    api(libs.kotlinx.coroutines.core.jvm)
    api(libs.kotlinx.coroutines.jdk8)
    api(libs.kotlinx.serialization.core)
    api(libs.kotlinx.serialization.json)
}

tasks.withType<Jar> {
    manifest.attributes(
        "FMLModType" to "LIBRARY",
        // Required for language providers
        "Implementation-Version" to version
    )
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "kfflang-neoforge"
        }
    }
}
