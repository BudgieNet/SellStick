plugins {
    java
    id("java-library")
    id("maven-publish")
    id("idea")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://ci.ender.zone/plugin/repository/everything/")
    }

    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.15.1")
    compileOnly("dev.jorel:commandapi-bukkit-core:10.0.1")
    compileOnly(libs.io.papermc.paper.paper.api)
    compileOnly(libs.com.github.milkbowl.vaultapi)
    compileOnly(libs.net.ess3.essentialsx)
    compileOnly(libs.com.github.brcdev.minecraft.shopgui.api)
}

group = "com.shmkane"
version = "2.0.0"
description = "sellstick"

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.register<Copy>("copyJarToServer") {
    dependsOn(tasks.build)
    from(layout.buildDirectory.file("libs/${rootProject.name}-${version}.jar"))
    into("/data/BudgieNet/PAPER_1_21_4/plugins/")
}

tasks.register<Exec>("restartPaper") {
    dependsOn(tasks.named("copyJarToServer"))
    commandLine("bash", "-c", "/data/BudgieNet/PAPER_1_21_4/reload_sellstick.sh")
    isIgnoreExitValue = true
}

tasks.named("build") {
    finalizedBy("copyJarToServer")
    finalizedBy("restartPaper")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

configurations.configureEach {
    resolutionStrategy.capabilitiesResolution.withCapability("com.destroystokyo.paper:paper-mojangapi") {
        select("net.budgie.papercrane:papercrane-api:1.21.4-R0.1-SNAPSHOT")
    }
}