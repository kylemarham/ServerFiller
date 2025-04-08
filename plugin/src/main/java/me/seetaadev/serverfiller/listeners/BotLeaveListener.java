package me.seetaadev.serverfiller.listeners;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.BotFactory;
import me.seetaadev.serverfiller.messages.MessageHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public class BotLeaveListener implements Listener {

    private final MessageHandler messageHandler;
    private final BotFactory botFactory;

    public BotLeaveListener(ServerFillerPlugin plugin) {
        this.botFactory = plugin.getBotFactory();
        this.messageHandler = plugin.getMessageHandler();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (botFactory.isBot(player.getUniqueId())) {
            event.quitMessage(messageHandler.format("leave", Map.of("bot_name", player.getName())));
        }
    }
}
