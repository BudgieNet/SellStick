import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "8.3.3"
}

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://ci.ender.zone/plugin/repository/everything/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven { url = uri("https://repo.essentialsx.net/releases/") }
    maven { url = uri("https://repo.codemc.io/repository/creatorfromhell/") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("de.tr7zw:item-nbt-api:2.15.5")
    implementation("dev.jorel:commandapi-paper-shade:11.1.0")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.18")
    compileOnly("net.essentialsx:EssentialsX:2.21.2")
    compileOnly("com.github.brcdev-minecraft:shopgui-api:3.2.0")
}

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }
tasks.withType<Javadoc> { options.encoding = "UTF-8" }

tasks.processResources {
    val props = mapOf(
        "name" to project.name,
        "version" to project.version,
        "description" to project.description,
        "author" to project.properties["author"],
        "main" to project.properties["mainClass"],
        "apiVersion" to project.properties["apiVersion"])
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.withType<ShadowJar> {
    relocate("de.tr7zw.changeme.nbtapi", project.properties["mainClass"] as String + ".nbtapi")
    relocate("dev.jorel.commandapi", project.properties["mainClass"] as String + ".commandapi")
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
    archiveFileName.set("${project.name}-${rootProject.version}.jar")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.build {
    dependsOn(tasks.clean)
    dependsOn(tasks.shadowJar)
}

tasks.compileJava {
    mustRunAfter(tasks.clean)
}

tasks.shadowJar {
    mustRunAfter(tasks.jar)
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

if (file("local.gradle.kts").exists()) {
    apply(from = "local.gradle.kts")
}
