package me.seetaadev.serverfiller.bot.service;

import me.seetaadev.serverfiller.bot.Bot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BotStorageService {

    private final Map<UUID, Bot> bots;

    public BotStorageService() {
        this.bots = new HashMap<>();
    }

    public void addBot(Bot bot) {
        bots.put(bot.getUniqueId(), bot);
    }

    public void removeBot(Bot bot) {
        bots.remove(bot.getUniqueId());
    }

    public Bot getBot(UUID uuid) {
        return bots.get(uuid);
    }

    public Bot getBot(String name) {
        return bots.values().stream()
                .filter(bot -> bot.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public Map<UUID, Bot> getBots() {
        return bots;
    }

    public List<Bot> offlineBots() {
        return bots.values().stream()
                .filter(bot -> !bot.isSpawned())
                .toList();
    }

    public List<Bot> onlineBots() {
        return bots.values().stream()
                .filter(Bot::isSpawned)
                .toList();
    }

    public boolean isOnline(String name) {
        return bots.values().stream()
                .filter(Bot::isSpawned)
                .anyMatch(bot -> bot.getName().equalsIgnoreCase(name));
    }
}
