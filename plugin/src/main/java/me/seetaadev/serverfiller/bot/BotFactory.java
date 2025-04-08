package me.seetaadev.serverfiller.bot;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.loader.BotConfigLoader;
import me.seetaadev.serverfiller.bot.service.BotStorageService;
import me.seetaadev.serverfiller.bot.settings.BotSettings;
import me.seetaadev.serverfiller.config.ConfigFile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class BotFactory {

    private final ServerFillerPlugin plugin;
    private final BotConfigLoader configLoader;
    private final BotStorageService botStorageService;
    private final BotBuilder botBuilder;

    public BotFactory(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        this.configLoader = new BotConfigLoader(plugin);
        this.botStorageService = new BotStorageService();
        this.botBuilder = new BotBuilder();
    }

    public void load() {
        ConfigFile configFile = new ConfigFile(plugin, null, "config", true);
        FileConfiguration config = configFile.getConfig();
        String hostname = config.getString("host.name", "localhost");
        int port = config.getInt("host.port", 25565);

        botBuilder.setPlugin(plugin);
        botBuilder.setHostname(hostname);
        botBuilder.setPort(port);

        File botsFolder = new File(plugin.getDataFolder(), "bots");
        if (!botsFolder.exists() && !botsFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create bots folder");
        }

        parseBotsFolder(botsFolder);
    }

    private void parseBotsFolder(File botsFolder) {
        File botsFile = new File(plugin.getDataFolder(), "bots.yml");
        if (!botsFile.exists()) {
            loadExistingBots(botsFolder);
            return;
        }

        ConfigFile botsConfigFile = new ConfigFile(plugin, null, "bots", false);
        FileConfiguration botsConfig = botsConfigFile.getConfig();

        ConfigurationSection botsSection = botsConfig.getConfigurationSection("bots");
        assert botsSection != null;
        for (String botID : botsSection.getKeys(false)) {
            String name = botsSection.getString(botID + ".name", botID);
            String rank = botsSection.getString(botID + ".rank", "default");
            int skillLevel = botsSection.getInt(botID + ".skillLevel", 1);

            ConfigFile botConfigFile = new ConfigFile(plugin, "bots", name, false);
            FileConfiguration botConfig = botConfigFile.getConfig();
            botConfig.set("rank", rank);
            botConfig.set("skillLevel", skillLevel);
            botConfigFile.save();
        }

        botsConfigFile.delete();
        loadExistingBots(botsFolder);
    }

    private void loadExistingBots(File botsFolder) {
        File[] botFiles = botsFolder.listFiles();
        if (botFiles != null) {
            for (File botFile : botFiles) {
                if (botFile.isFile() && botFile.getName().endsWith(".yml")) {
                    String name = botFile.getName().replace(".yml", "");
                    if (botStorageService.isOnline(name)) continue;

                    BotSettings settings = configLoader.loadSettings(name, false);
                    if (settings != null) {
                        Bot bot = botBuilder.createBot(settings);
                        botStorageService.addBot(bot);
                    } else {
                        plugin.getLogger().warning("Failed to load bot settings from " + name);
                    }
                }
            }
        }
    }

    public void reload() {
        load();
    }

    public Bot getBot(String name) {
        return botStorageService.getBot(name);
    }

    public boolean isBot(UUID uuid) {
        return botStorageService.getBots().containsKey(uuid);
    }

    public Map<UUID, Bot> getBots() {
        return botStorageService.getBots();
    }

    public Bot randomOnlineBot(boolean ignoreCooldown) {
        if (ignoreCooldown) {
            List<Bot> bots = botStorageService.onlineBots();
            if (bots.isEmpty()) return null;

            Random random = new Random();
            return bots.get(random.nextInt(bots.size()));
        }

        List<Bot> bots = botStorageService.onlineBots()
                .stream()
                .filter(bot -> !bot.isInCooldown())
                .toList();

        if (bots.isEmpty()) return null;
        Random random = new Random();
        return bots.get(random.nextInt(bots.size()));
    }

    public Bot randomOfflineBot() {
        List<Bot> bots = botStorageService.offlineBots();
        if (bots.isEmpty()) return null;

        Random random = new Random();
        return bots.get(random.nextInt(bots.size()));
    }

    public List<Bot> getOnlineBots() {
        return botStorageService.onlineBots();
    }

    public boolean isEmpty() {
        return botStorageService.onlineBots().isEmpty();
    }

    public int onlineBotsCount() {
        return botStorageService.onlineBots().size();
    }

    public Bot loadBot(BotSettings settings) {
        Bot bot = botBuilder.createBot(settings);
        botStorageService.addBot(bot);
        return bot;
    }
}

