import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("project.common-conventions")
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

tasks {
    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
    }

    named<DefaultTask>("build") {
        dependsOn("shadowJar")
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("org.json:json:20230618")
}

bukkit {
    val projectName = "${findProperty("plugin-name")}"

    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "me.seetaadev.${projectName.lowercase()}.${projectName}Plugin"
    apiVersion = "1.21"
    version = "${project.version}"
    authors = listOf("SeeTaaDev")
    description = "${findProperty("plugin-description")}"
    name = projectName
    depend = listOf("LuckPerms", "PlaceholderAPI")
    commands {
        register("serverfiller") {
            description = "ServerFiller command"
            aliases = listOf("sf")
        }
    }
}
