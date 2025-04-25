package me.seetaadev.serverfiller.listeners;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.SchedulerUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.Bot;
import me.seetaadev.serverfiller.bot.BotBuilder;
import me.seetaadev.serverfiller.bot.BotFactory;
import me.seetaadev.serverfiller.messages.MessageHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BotJoinListener implements Listener {

    private final MessageHandler messageHandler;
    private final BotFactory botFactory;
    private final ServerFillerPlugin plugin;
    private final BotBuilder botBuilder;

    public BotJoinListener(ServerFillerPlugin plugin) {
        this.plugin = plugin; // Assign the plugin instance
        this.botFactory = plugin.getBotFactory();
        this.messageHandler = plugin.getMessageHandler();
        this.botBuilder = botFactory.botBuilder();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Random rand = new Random();

        // If the joining player is a bot, send a join message.

        if (botFactory.isBot(player.getUniqueId())) {
            event.joinMessage(messageHandler.format("join", Map.of("bot_name", player.getName())));

            if(!botBuilder.isShouldWeWelcomeBots()) {
                return;
            }
        }

        boolean enabled = botBuilder.isChatWelcomeEnabled();
        double welcomeChance = botBuilder.getWelcomeChance();

        int minWelcomes = botBuilder.getWelcomeMin();
        int maxWelcomes = botBuilder.getWelcomeMax();

        // Get all online bots from the botFactory.
        List<Bot> onlineBots = botFactory.getOnlineBots();
        List<Bot> welcomeBots = new ArrayList<>();

        if(!enabled) {
            return;
        }

        // First, loop through online bots and select those that pass the random chance.
        for (Bot bot : onlineBots) {
            if (rand.nextDouble() < welcomeChance) {
                welcomeBots.add(bot);
            }
        }

        // Ensure that at least 'minWelcomes' bots are selected.
        if (welcomeBots.size() < minWelcomes) {
            for (Bot bot : onlineBots) {
                if (!welcomeBots.contains(bot)) {
                    welcomeBots.add(bot);
                    if (welcomeBots.size() >= minWelcomes) {
                        break;
                    }
                }
            }
        }

        // If we have too many, randomly trim the list to 'maxWelcomes'.
        if (welcomeBots.size() > maxWelcomes) {
            Collections.shuffle(welcomeBots, rand);
            while (welcomeBots.size() > maxWelcomes) {
                welcomeBots.removeLast();
            }
        }

        // Loop through the selected bots and send their welcome messages with a random delay.
        for (Bot bot : welcomeBots) {
            String message;
            if (!player.hasPlayedBefore()) {
                message = bot.randomFirstJoinMessage();
            } else {
                message = bot.randomWelcomeMessage();
            }
            if (message != null) {
                final String finalMessage = message.replace("{player_name}", player.getName());
                int delay = rand.nextInt(191) + 10;  // random delay between 10 and 200 ticks
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    sendBotReply(bot, finalMessage);
                }, delay);
            }
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
        sendDiscordMessage(replyText, bot);
    }

    public void sendDiscordMessage(String message, Bot bot) {
        SchedulerUtil.runTaskAsynchronously(DiscordSRV.getPlugin(), () -> {
            DiscordSRV.getPlugin().processChatMessage(
                    bot,
                    message,
                    DiscordSRV.getPlugin().getOptionalChannel("global"),
                    false, null);
        });
    }
}
