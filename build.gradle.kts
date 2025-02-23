import java.util.*

plugins {
    kotlin("jvm") version "2.1.10"
    `maven-publish`
}

group = "cc.modlabs"

version = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin")).run {
    "${get(Calendar.YEAR)}.${"%02d".format(get(Calendar.MONTH) + 1)}.${"%02d".format(get(Calendar.DAY_OF_MONTH))}"
}

repositories {
    maven("https://nexus.flawcra.cc/repository/maven-mirrors/")
}

dependencies {
    testImplementation(kotlin("test"))

    api("ch.qos.logback:logback-classic:1.5.6")
    api("io.github.cdimascio:dotenv-kotlin:6.4.1")
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
                username = System.getenv("FLAWCRA_REPO_USER")
                password = System.getenv("FLAWCRA_REPO_KEY")
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