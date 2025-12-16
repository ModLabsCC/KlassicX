import java.util.*

plugins {
    kotlin("jvm") version "2.1.20"
    `java-library`
    `maven-publish`
    kotlin("plugin.serialization") version "2.1.20"
    id("org.sonarqube") version "7.0.1.6134"
}

sonar {
    properties {
        property("sonar.projectKey", "ModLabsCC_KlassicX_9255672f-a744-49cf-84be-f3d2ecfcda02")
        property("sonar.projectName", "KlassicX")
    }
}

group = "cc.modlabs"

version = System.getenv("VERSION_OVERRIDE") ?: Calendar.getInstance(TimeZone.getTimeZone("UTC")).run {
    "${get(Calendar.YEAR)}.${get(Calendar.MONTH) + 1}.${get(Calendar.DAY_OF_MONTH)}.${String.format("%02d%02d", get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))}"
}

repositories {
    maven("https://nexus.modlabs.cc/repository/maven-mirrors/")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    api("ch.qos.logback:logback-classic:1.5.22")
    api("io.github.cdimascio:dotenv-kotlin:6.5.1")
    api("dev.fruxz:ascend:2025.7-8af65e5")
    api("com.google.code.gson:gson:2.13.1")

    api("org.jetbrains.kotlin:kotlin-reflect:2.1.20")
    api("com.google.guava:guava:33.4.8-jre")
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
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
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
            from(components["java"])

            artifact(tasks.named("sourcesJar"))

            pom {
                name.set("KlassicX")
                description.set("A utility library designed to simplify development with Kotlin.")
                url.set("https://github.com/ModLabsCC/KlassicX")
                licenses {
                    license {
                        name.set("GPL-3.0")
                        url.set("https://github.com/ModLabsCC/KlassicX/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("ModLabsCC")
                        name.set("ModLabsCC")
                        email.set("contact@modlabs.cc")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/ModLabsCC/KlassicX.git")
                    developerConnection.set("scm:git:git@github.com:ModLabsCC/KlassicX.git")
                    url.set("https://github.com/ModLabsCC/KlassicX")
                }
            }
        }
    }
}
