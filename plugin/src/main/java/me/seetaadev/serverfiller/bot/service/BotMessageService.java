package me.seetaadev.serverfiller.bot.service;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.config.ConfigFile;

import java.util.List;

public class BotMessageService {

    private final ServerFillerPlugin plugin;
    private boolean enabled;
    private Double chance;

    public BotMessageService(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        ConfigFile config = new ConfigFile(plugin, null, "config", true);
        enabled = config.getConfig().getBoolean("chat.welcome.enabled", true);
        chance = config.getConfig().getDouble("chat.welcome.chance", 0.5);
    }

    public void reload() {
        load();
    }

    public String randomWelcomeMessage() {
        if (!isEnabled() || !isChance()) {
            return null;
        }

        return getRandomMessage(plugin.getMessageHandler().getMessageList("chat.welcome_message"));
    }

    public String randomFirstJoinMessage() {
        if (!isEnabled() || !isChance()) {
            return null;
        }

        return getRandomMessage(plugin.getMessageHandler().getMessageList("chat.first_message"));
    }

    private String getRandomMessage(List<String> messages) {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get((int) (Math.random() * messages.size()));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isChance() {
        return Math.random() < chance;
    }
}
