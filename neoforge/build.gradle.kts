import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.forgegradle)
    `maven-publish`
    java
}

val mc_version: String by project

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

jarJar.enable()

configurations {
    apiElements {
        artifacts.clear()
    }
    runtimeElements {
        // Only include the subprojects as transitive runtime dependencies
        setExtendsFrom(hashSetOf(configurations.getByName("api")))
        // Publish the jarJar ONLY
        artifacts.clear()
        outgoing.artifact(tasks.jarJar)
    }
}

extensions.getByType(net.minecraftforge.gradle.userdev.UserDevExtension::class).apply {
    mappings("official", mc_version)
}

dependencies {
    minecraft(libs.forge)

    jarJarLib(libs.kotlin.reflect)
    jarJarLib(libs.kotlin.stdlib.asProvider())
    jarJarLib(libs.kotlin.stdlib.jdk7)
    jarJarLib(libs.kotlin.stdlib.jdk8)
    jarJarLib(libs.kotlinx.coroutines.core.asProvider())
    jarJarLib(libs.kotlinx.coroutines.core.jvm)
    jarJarLib(libs.kotlinx.coroutines.jdk8)
    jarJarLib(libs.kotlinx.serialization.core)
    jarJarLib(libs.kotlinx.serialization.json)

    // KFF Modules
    api(projects.neoforge.kfflang)
    api(projects.neoforge.kfflib)
    api(projects.neoforge.kffmod)
}

// maven.repo.local is set within the Julia script in the website branch
tasks.create("publishAllMavens") {
    dependsOn(":neoforge:publishToMavenLocal")
    dependsOn(":neoforge:kfflib:publishToMavenLocal")
    dependsOn(":neoforge:kfflang:publishToMavenLocal")
    dependsOn(":neoforge:kffmod:publishToMavenLocal")
}

fun DependencyHandler.jarJarLib(dependencyNotation: Provider<out ExternalModuleDependency>) {
    val dep = dependencyNotation.get().copy()
    jarJar("${dep.group}:${dep.name}:[${dep.version},)") {
        jarJar.pin(this, dep.version!!)
        isTransitive = false
    }
}

tasks {
    jarJar.configure {
        manifest {
            attributes(
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge",
                "FMLModType" to "LIBRARY"
            )
        }
    }

    whenTaskAdded {
        // Disable reobfJar
        if (name == "reobfJar") {
            enabled = false
        }
        // Fight ForgeGradle and Forge crashing when MOD_CLASSES don't exist
        if (name == "prepareRuns") {
            doFirst {
                sourceSets.main.get().output.files.forEach(File::mkdirs)
            }
        }
    }

    withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    assemble {
        dependsOn(jarJar)
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "kotlinforforge-neoforge"

            artifact(tasks.jar) {
                classifier = "slim"
            }
        }
    }
}

fun DependencyHandler.minecraft(dependencyNotation: Any): Dependency? = add("minecraft", dependencyNotation)
