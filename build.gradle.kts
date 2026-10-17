plugins {
    id("com.budgienet.paper-plugin")
}

dependencies {
    implementation(libs.commandapi)
    implementation(libs.nbtapi)
    compileOnly(libs.vault.unlocked)
    compileOnly(libs.essentialsx)
    compileOnly("com.github.brcdev-minecraft:shopgui-api:3.2.0")
}
