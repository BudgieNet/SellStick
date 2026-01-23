import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "8.3.3"
}

group = "com.shmkane"
version = "2.1.0"
description = "SellStick"

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

repositories {
    mavenLocal()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://ci.ender.zone/plugin/repository/everything/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven { url = uri("https://repo.essentialsx.net/releases/") }
    maven { url = uri("https://repo.codemc.io/repository/creatorfromhell/") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.15.5")
    implementation("dev.jorel:commandapi-paper-shade:11.1.0")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.18")
    compileOnly("net.essentialsx:EssentialsX:2.21.2") {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }
    compileOnly("com.github.brcdev-minecraft:shopgui-api:3.2.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.withType<ShadowJar> {
    relocate("dev.jorel.commandapi", "com.shmkane.sellstick.commandapi")
    archiveFileName.set("${project.name}-${rootProject.version}.jar")
}

tasks.register<Copy> ("copyJarToServer") {
    dependsOn(":jar")
    dependsOn(tasks.shadowJar)
    from(layout.buildDirectory.file("libs/${rootProject.name}-${rootProject.version}.jar"))
    into("/data/BudgieNet/PAPER_1_21_11/plugins/")
}

tasks.register<Exec> ("restartPaper") {
    dependsOn(tasks.named("copyJarToServer"))
    commandLine("bash", "-c", "pgrep -f 'paper.jar' | xargs kill")
    isIgnoreExitValue = true
}

tasks.shadowJar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}