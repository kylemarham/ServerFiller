package me.seetaadev.serverfiller.bot.responses.local;

import me.clip.placeholderapi.PlaceholderAPI;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.Bot;
import me.seetaadev.serverfiller.bot.BotFactory;
import me.seetaadev.serverfiller.bot.personality.Personality;
import me.seetaadev.serverfiller.bot.responses.ai.AIChatResponder;
import me.seetaadev.serverfiller.config.ConfigFile;
import me.seetaadev.serverfiller.hooks.HookManager;
import me.seetaadev.serverfiller.messages.MessageHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ChatResponder {

    private final ServerFillerPlugin plugin;
    private final BotFactory botFactory;
    private final MessageHandler messageHandler;
    private final AIChatResponder AIChatResponder;
    private final Map<String, ResponseCategory> categories = new HashMap<>();
    private boolean chatEnabled;
    private final HookManager hook;

    public ChatResponder(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        this.botFactory = plugin.getBotFactory();
        this.messageHandler = plugin.getMessageHandler();
        this.AIChatResponder = new AIChatResponder(plugin);
        this.hook = plugin.getHookManager();
    }

    public void load() {
        ConfigFile responsesConfigFile = new ConfigFile(plugin, null, "responses", true);
        FileConfiguration responsesConfig = responsesConfigFile.getConfig();

        for (String categoryName : responsesConfig.getKeys(false)) {
            List<String> keywords = responsesConfig.getStringList(categoryName + ".keywords");
            List<String> responses = responsesConfig.getStringList(categoryName + ".responses");
            ResponseCategory category = new ResponseCategory(keywords, responses);
            categories.put(categoryName, category);
        }

        ConfigFile configFile = new ConfigFile(plugin, null, "config", true);
        FileConfiguration config = configFile.getConfig();
        chatEnabled = config.getBoolean("bot.chatEnabled", true);
        AIChatResponder.load();
    }

    public void reload() {
        categories.clear();
        load();
        AIChatResponder.reload();
    }

    public void sendResponse(String message, Player player) {
        if(!chatEnabled) {
            return;
        }

        Bot bot = matchesBot(message);

        for (ResponseCategory category : categories.values()) {
            if (category.matches(message)) {
                if (bot != null) {
                    String response = category.getRandomResponse().replace("{player_name}", player.getName());
                    int delaySeconds = ThreadLocalRandom.current().nextInt(AIChatResponder.getMinDelay(), AIChatResponder.getMaxDelay() + 1);
                    bot.processResponse();
                    bot.startCooldown();
                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            sendBotReply(bot, response), delaySeconds * 20L);
                } else {
                    Bot newBot = botFactory.randomOnlineBot(false);
                    String response = category.getRandomResponse().replace("{player_name}", player.getName());
                    int delaySeconds = ThreadLocalRandom.current().nextInt(AIChatResponder.getMinDelay(), AIChatResponder.getMaxDelay() + 1);
                    newBot.processResponse();
                    newBot.startCooldown();
                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            sendBotReply(newBot, response), delaySeconds * 20L);
                }
                return;
            }
        }

         sendAIResponse(message, bot);
    }

    public Bot matchesBot(String message) {
        List<Bot> onlineBots = botFactory.getOnlineBots();
        for (Bot bot : onlineBots) {
            if (message.contains(bot.getName())) {
                return bot;
            }
        }

        return null;
    }

    public void sendAIResponse(String message, Bot directBot) {
        if(!chatEnabled) {
            return;
        }

        if (AIChatResponder.isAIEnabled() && AIChatResponder.getAPIKey() != null) {
            plugin.getAsyncExecutor().execute(() -> {
                if (botFactory.isEmpty()) return;
                try {
                    Personality chosenAssistant = plugin.getPersonalityManager().getRandomPersonality();
                    List<String> aiReplies = AIChatResponder.getResponses(message, chosenAssistant.getSystemPrompt(), 5, AIChatResponder.getAPIKey());
                    if (aiReplies.isEmpty()) return;

                    int replyCount = ThreadLocalRandom.current().nextInt(AIChatResponder.getMinReplies(), AIChatResponder.getMaxReplies() + 1);
                    int responsesToSend = Math.min(replyCount, aiReplies.size());

                    if (directBot == null) {
                        for (int i = 0; i < responsesToSend; i++) {
                            Bot bot = botFactory.randomOnlineBot(false);
                            if (bot == null) continue;

                            bot.processResponse();
                            bot.startCooldown();
                            String reply = aiReplies.get(i).trim();
                            int delaySeconds = ThreadLocalRandom.current().nextInt(AIChatResponder.getMinDelay(), AIChatResponder.getMaxDelay() + 1);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> sendBotReply(bot, reply), delaySeconds * 20L);
                        }
                    } else {
                        directBot.processResponse();
                        directBot.startCooldown();
                        String reply = aiReplies.getFirst().trim();
                        int delaySeconds = ThreadLocalRandom.current().nextInt(AIChatResponder.getMinDelay(), AIChatResponder.getMaxDelay() + 1);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> sendBotReply(directBot, reply), delaySeconds * 20L);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error querying Together AI: " + e.getMessage());
                }
            });
        }
    }

    private void sendBotReply(Bot bot, String replyText) {
        String botFormat = messageHandler.getMessage("chat.bot");
        botFormat = botFormat.replace("{displayName}", bot.getName())
                .replace("{botSkillLevel}", String.valueOf(bot.getSettings().getSkillLevel()))
                .replace("{message}", replyText);
        botFormat = PlaceholderAPI.setPlaceholders(bot, botFormat);
        Component botResponse = messageHandler.format(botFormat);
        Bukkit.broadcast(botResponse);
        bot.processResponse();
        if (hook != null)
            hook.sendDiscordMessage(replyText, bot);
    }



    public AIChatResponder getAIChatResponder() {
        return AIChatResponder;
    }
}
