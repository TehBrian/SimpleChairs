plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("net.kyori.indra.checkstyle") version "3.0.1"
    id("com.github.ben-manes.versions") version "0.44.0"
}

group = "xyz.tehbrian"
version = "0.2.0-SNAPSHOT"
description = "Allows players to sit on chairs, such as stairs or slabs."

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    processResources {
        expand("version" to project.version, "description" to project.description)
    }

    base {
        archivesName.set("SimpleChairs")
    }

    shadowJar {
        archiveClassifier.set("")
    }

    runServer {
        minecraftVersion("1.19.3")
    }
}
