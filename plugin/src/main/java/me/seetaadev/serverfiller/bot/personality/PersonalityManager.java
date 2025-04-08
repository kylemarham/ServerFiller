package me.seetaadev.serverfiller.bot.personality;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.config.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class PersonalityManager {

    private final ServerFillerPlugin plugin;
    private final List<Personality> personalities;

    public PersonalityManager(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        this.personalities = new ArrayList<>();
    }

    public void load() {
        ConfigFile configFile = new ConfigFile(plugin, null, "personalities", true);
        FileConfiguration config = configFile.getConfig();

        for (String key : config.getKeys(false)) {
            String path = key + ".";
            double chance = config.getDouble(path + "chance", 0.5);
            String tone = config.getString(path + "tone", "neutral");
            String vocabulary = config.getString(path + "vocabulary", "simple");
            String prompt = config.getString(path + "systemPrompt", "You are a helpful Minecraft assistant. Reply concisely, using short, friendly messages without emojis.");
            personalities.add(new Personality(key, chance, tone, vocabulary, prompt));
        }
    }

    public void reload() {
        personalities.clear();
        load();
    }

    public Personality getRandomPersonality() {
        double totalChance = 0;
        for (Personality personality : personalities) {
            totalChance += personality.getChance();
        }

        double randomValue = Math.random() * totalChance;
        for (Personality personality : personalities) {
            if (randomValue < personality.getChance()) {
                return personality;
            }
            randomValue -= personality.getChance();
        }

        return personalities.getFirst();
    }
}
