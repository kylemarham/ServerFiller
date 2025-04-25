package me.seetaadev.serverfiller.bot.loader;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.settings.BotSettings;
import me.seetaadev.serverfiller.config.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
        boolean hasPlayedBefore = config.getBoolean("hasPlayedBefore", false);

        String uuidString = config.getString("uuid", null);
        UUID uuid = (uuidString != null) ? UUID.fromString(uuidString) : null;

        return new BotSettings(id, rank, skillLevel, uuid, hasPlayedBefore);
    }

    public CompletableFuture<BotSettings> loadSettingsAsync(String id, boolean copy) {
        return CompletableFuture.supplyAsync(() -> loadSettings(id, copy));
    }
}
