plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "1.1.0"
    id("net.kyori.indra.checkstyle") version "2.1.1"
}

group = "xyz.tehbrian"
version = "0.2.0-SNAPSHOT"
description = "Allows players to sit on chairs, such as stairs or slabs."

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19-R0.1-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    processResources {
        expand("version" to project.version, "description" to project.description)
    }

    shadowJar {
        archiveBaseName.set("SimpleChairs")
        archiveClassifier.set("")
    }

    runServer {
        minecraftVersion("1.19")
    }
}
