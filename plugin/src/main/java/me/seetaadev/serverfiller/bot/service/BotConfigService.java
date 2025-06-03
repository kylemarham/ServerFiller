package me.seetaadev.serverfiller.bot.service;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.settings.BotSettings;
import me.seetaadev.serverfiller.config.ConfigFile;

public class BotConfigService {
    private final ConfigFile configFile;

    public BotConfigService(ServerFillerPlugin plugin, BotSettings settings) {
        this.configFile = new ConfigFile(plugin, "bots", settings.getName(), false);
    }

    public void save(BotSettings settings) {
        configFile.getConfig().set("rank", settings.getRank());
        configFile.getConfig().set("uuid", settings.getUUID().toString());
        configFile.getConfig().set("skillLevel", settings.getSkillLevel());
        configFile.getConfig().set("hasPlayedBefore", settings.hasPlayedBefore());
        configFile.save();
    }
}
