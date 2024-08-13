import java.util.*

plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "com.liamxsage"

version = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin")).run {
    "${get(Calendar.YEAR)}.${get(Calendar.MONTH)}.${get(Calendar.DAY_OF_MONTH)}-${get(Calendar.HOUR_OF_DAY)}.${get(Calendar.MINUTE)}"
}

repositories {
    maven("https://nexus.flawcra.cc/repository/maven-mirrors/")
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1",)
}

tasks {
    test {
        useJUnitPlatform()
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
            name = "FlawcraNexus"
            url = uri("https://nexus.flawcra.cc/repository/maven-public/")
            credentials {
                username = System.getenv("FLAWCRA_REPO_USER")
                password = System.getenv("FLAWCRA_REPO_KEY")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
}