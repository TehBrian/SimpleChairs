plugins {
  id("java")
  id("com.github.johnrengelman.shadow") version "8.1.1"
  id("xyz.jpenilla.run-paper") version "2.3.1"
  id("net.kyori.indra.checkstyle") version "3.1.3"
  id("com.github.ben-manes.versions") version "0.51.0"
}

group = "dev.tehbrian"
version = "0.2.2"
description = "Allows players to sit on chairs, such as stairs or slabs."

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
  mavenCentral()
  maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
  compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
}

tasks {
  assemble {
    dependsOn(shadowJar)
  }

  processResources {
    filesMatching("plugin.yml") {
      expand(
        "version" to project.version,
        "description" to project.description
      )
    }
  }

  base {
    archivesName.set("SimpleChairs")
  }

  shadowJar {
    archiveClassifier.set("")
  }

  runServer {
    minecraftVersion("1.21.1")
  }
}
