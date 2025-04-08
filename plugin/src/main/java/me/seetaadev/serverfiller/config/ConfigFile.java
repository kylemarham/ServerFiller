package me.seetaadev.serverfiller.config;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigFile {
    private final ServerFillerPlugin plugin;
    private final File file;
    private final FileConfiguration config;

    public ConfigFile(ServerFillerPlugin plugin, String folder, String name, boolean copy) {
        if (folder != null) {
            folder = "/" + folder;

            File folderFile = new File(plugin.getDataFolder() + folder);
            if (!folderFile.exists()) {
                if (!folderFile.mkdir()) {
                    plugin.getComponentLogger().error(Component.text("Could not create " + folderFile.getName() + " folder!"));
                }
            }

            file = new File(plugin.getDataFolder() + folder, name + ".yml");

        } else {
            file = new File(plugin.getDataFolder(), name + ".yml");

        }
        if (!file.exists()) {
            try {
                if (!file.exists() && copy) {
                    if (folder != null) {
                        plugin.saveResource(folder.substring(1) + "/" + name + ".yml", false);
                    } else {
                        plugin.saveResource(name + ".yml", false);
                    }
                } else {
                    if (!file.createNewFile())
                        plugin.getComponentLogger().error(Component.text("Could not create, new file " + file.getName()));
                }
            } catch (IOException e) {
                plugin.getComponentLogger().error(Component.text("Could not create " + file.getName()));
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        this.plugin = plugin;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e2) {
            plugin.getComponentLogger().error(Component.text("Could not save " + file.getName() + ".yml!"));
        }
    }

    public void reload() {
        try {
            config.load(file);
        } catch (Exception e) {
            plugin.getComponentLogger().error(Component.text("Could not reload " + file.getName() + ".yml!"));
        }
    }

    public void delete() {
        if (!file.delete())
            plugin.getComponentLogger().error(Component.text("Could not delete " + file.getName() + ".yml!"));
    }
}
