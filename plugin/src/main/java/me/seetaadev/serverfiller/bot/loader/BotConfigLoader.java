package me.seetaadev.serverfiller.bot.loader;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.settings.BotSettings;
import me.seetaadev.serverfiller.config.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class BotConfigLoader {

    private final ServerFillerPlugin plugin;

    public BotConfigLoader(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    public BotSettings loadSettings(String id, boolean copy) {
        ConfigFile configFile = new ConfigFile(plugin, "bots", id, copy);
        FileConfiguration config = configFile.getConfig();
        String rank = config.getString("rank");
        int skillLevel = config.getInt("skillLevel");

        String uuidString = config.getString("uuid", null);
        UUID uuid = (uuidString != null) ? UUID.fromString(uuidString) : null;

        return new BotSettings(id, rank, skillLevel, uuid);
    }
}
