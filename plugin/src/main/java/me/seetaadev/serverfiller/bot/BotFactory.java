package me.seetaadev.serverfiller.bot;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.loader.BotConfigLoader;
import me.seetaadev.serverfiller.bot.service.BotStorageService;
import me.seetaadev.serverfiller.bot.settings.BotSettings;
import me.seetaadev.serverfiller.config.ConfigFile;
import org.bukkit.Bukkit;
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
        this.botBuilder = new BotBuilder(plugin);
    }

    public void load() {
        ConfigFile configFile = new ConfigFile(plugin, null, "config", true);
        FileConfiguration config = configFile.getConfig();
        String hostname = config.getString("host.name", "localhost");
        int port = config.getInt("host.port", 25565);
        boolean skinsEnabled = config.getBoolean("bots.skinsEnabled", true);
        boolean shouldWeWelcomeBots = config.getBoolean("chat.botWelcomeEnabled", true);
        boolean chatWelcomeEnabled = config.getBoolean("chat.welcome.enabled", true);
        double chatWelcomeChance = config.getDouble("chat.welcome.chance", 0.2);
        int welcomeMin = config.getInt("chat.welcome.min", 1);
        int welcomeMax = config.getInt("chat.welcome.max", 5);

        botBuilder.setHostname(hostname);
        botBuilder.setPort(port);
        botBuilder.setSkinsEnabled(skinsEnabled);
        botBuilder.setShouldWeWelcomeBots(shouldWeWelcomeBots);
        botBuilder.setChatWelcomeEnabled(chatWelcomeEnabled);
        botBuilder.setWelcomeChance(chatWelcomeChance);
        botBuilder.setWelcomeMin(welcomeMin);
        botBuilder.setWelcomeMax(welcomeMax);

        File botsFolder = new File(plugin.getDataFolder(), "bots");
        if (!botsFolder.exists() && !botsFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create bots folder");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> parseBotsFolder(botsFolder));
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
            botConfig.set("hasPlayedBefore", false);
            botConfigFile.save();
        }

        botsConfigFile.delete();
        loadExistingBots(botsFolder);
    }

    private void loadExistingBots(File botsFolder) {
        File[] botFiles = botsFolder.listFiles();
        if (botFiles == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (File botFile : botFiles) {
                if (!botFile.isFile() || !botFile.getName().endsWith(".yml")) continue;

                String name = botFile.getName().replace(".yml", "");
                if (botStorageService.isOnline(name)) continue;

                configLoader.loadSettingsAsync(name, false).thenAccept(settings -> {
                    if (settings != null)
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Bot bot = botBuilder.createBot(settings);
                            botStorageService.addBot(bot);
                        });
                });
            }
        });
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

    public BotBuilder botBuilder() {
        return botBuilder;
    }
}

