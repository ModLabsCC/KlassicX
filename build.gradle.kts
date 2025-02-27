import java.util.*

plugins {
    kotlin("jvm") version "2.1.10"
    `maven-publish`
    kotlin("plugin.serialization") version "2.1.0"
}

group = "cc.modlabs"

version = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin")).run {
    "${get(Calendar.YEAR)}.${"%02d".format(get(Calendar.MONTH) + 1)}.${"%02d".format(get(Calendar.DAY_OF_MONTH))}-${"%02d".format(get(Calendar.HOUR_OF_DAY))}.${"%02d".format(get(Calendar.MINUTE))}"
}

repositories {
    maven("https://nexus.modlabs.cc/repository/maven-mirrors/")
}

dependencies {
    testImplementation(kotlin("test"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    api("ch.qos.logback:logback-classic:1.5.6")
    api("io.github.cdimascio:dotenv-kotlin:6.4.1")
    api("dev.fruxz:ascend:2024.2.2")
    api("com.google.code.gson:gson:2.11.0")
}

tasks {
    test {
        useJUnitPlatform()
    }

    register<Jar>("sourcesJar") {
        description = "Generates the sources jar for this project."
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }
}

kotlin {
    jvmToolchain(23)
    compilerOptions {
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(23))
}

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "ModLabsNexus"
            url = uri("https://nexus.modlabs.cc/repository/maven-public/")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.named("jar").get()) {
                classifier = null
            }

            artifact(tasks.named("sourcesJar"))

            pom {
                name.set("KlassicX")
                description.set("A utility library designed to simplify development with Kotlin.")
            }
        }
    }
}
