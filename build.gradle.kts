plugins {
    `java-library`
}

group = "com.barfl"
version = "1.0.0"
description = "Spigot/Paper port of the Treecutters DiamondFire minigame"

base {
    archivesName.set("Treecutters")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
    maven("https://maven.enginehub.org/repo/") {
        name = "enginehub-maven"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // WorldEdit's Gradle metadata pins guava/gson/fastutil to the exact versions the Minecraft
    // server itself bundles ("Mojang provides X"), which conflicts with the newer versions
    // Paper API's own metadata requests. Since this is compileOnly (the real versions come from
    // the server at runtime either way), excluding them here just lets Paper API's requested
    // versions win instead of the two conflicting.
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.9") {
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.google.code.gson", module = "gson")
        exclude(group = "it.unimi.dsi", module = "fastutil")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    archiveVersion.set(project.version.toString())
}
