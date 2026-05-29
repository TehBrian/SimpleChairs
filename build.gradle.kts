plugins {
	id("java")
	id("com.gradleup.shadow") version "9.4.2"
	id("xyz.jpenilla.run-paper") version "3.0.2"
	id("net.kyori.indra.checkstyle") version "4.0.0"
	id("com.github.ben-manes.versions") version "0.54.0"
}

group = "dev.tehbrian"
version = "0.3.0"
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
	compileOnly("io.papermc.paper:paper-api:26.1.2.build.66-stable")
	compileOnly("org.jspecify:jspecify:1.0.0")
	implementation("dev.tehbrian:agna-paper:1.0.0") {
		exclude(group = "org.spongepowered", module = "configurate-core")
	}
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
		minecraftVersion("26.1.2")
	}
}
