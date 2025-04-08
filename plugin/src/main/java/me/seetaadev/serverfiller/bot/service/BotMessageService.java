package me.seetaadev.serverfiller.bot.service;

import me.seetaadev.serverfiller.ServerFillerPlugin;

import java.util.List;

public class BotMessageService {

    private final ServerFillerPlugin plugin;
    private boolean enabled;
    private Double chance;


    public BotMessageService(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        enabled = plugin.getConfig().getBoolean("chat.welcome.enabled", true);
        chance = plugin.getConfig().getDouble("chat.welcome.chance", 0.5);
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
