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
    compileOnly("com.bencodez:votingplugin:6.18.3")
    compileOnly("com.discordsrv:discordsrv:1.28.0")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.9.5-SNAPSHOT")
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
    depend = listOf("PlaceholderAPI")
    softDepend = listOf("VotingPlugin", "DiscordSRV", "LuckPerms", "MythicLib", "MMOItems")
    commands {
        register("serverfiller") {
            description = "ServerFiller command"
            aliases = listOf("sf")
        }
    }
}
