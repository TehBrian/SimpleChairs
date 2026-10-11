plugins {
	id("java")
	id("com.gradleup.shadow") version "9.6.1"
	id("xyz.jpenilla.run-paper") version "3.1.0"
	id("net.kyori.indra.checkstyle") version "4.1.0"
	id("io.github.ben-manes.versions") version "0.65.0"
}

group = "dev.tehbrian"
version = "0.4.1"
description = "Allows players to sit on chairs, such as stairs or slabs."

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
	mavenCentral()
	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://repo.tehbrian.dev/releases/")
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:26.3.build.+")
	compileOnly("org.jspecify:jspecify:1.0.1")
	implementation("dev.tehbrian:agna-paper:1.2.4")
	implementation("org.bstats:bstats-bukkit:3.2.1")
}

tasks {
	assemble {
		dependsOn(shadowJar)
	}

	processResources {
		filesMatching("plugin.yml") {
			expand(
					mapOf(
							"version" to project.version,
							"description" to project.description
					)
			)
		}
	}

	base {
		archivesName.set("SimpleChairs")
	}

	shadowJar {
		archiveClassifier.set("")

		val libsPackage = "${project.group}.${project.name}.libs"
		fun moveToLibs(vararg patterns: String) {
			for (pattern in patterns) {
				relocate(pattern, "$libsPackage.$pattern")
			}
		}

		moveToLibs(
				"dev.tehbrian.agna",
				"org.bstats",
		)
	}

	runServer {
		minecraftVersion("26.3")
	}
}
