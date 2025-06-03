package me.seetaadev.serverfiller.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.responses.local.ChatResponder;
import me.seetaadev.serverfiller.messages.MessageHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final ServerFillerPlugin plugin;
    private final MessageHandler messageHandler;
    private final ChatResponder chatResponder;
    private final PlainTextComponentSerializer plainTextSerializer = PlainTextComponentSerializer.plainText();

    public ChatListener(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        this.messageHandler = plugin.getMessageHandler();
        this.chatResponder = plugin.getChatResponder();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = getOriginalMessage(event.message());

        String playerFormat = messageHandler.getMessage("chat.player");
        playerFormat = playerFormat.replace("{displayName}", player.getName())
                .replace("{message}", message);
        playerFormat = PlaceholderAPI.setPlaceholders(player, playerFormat);
        Component formattedMessage = messageHandler.format(playerFormat);
//        event.setCancelled(true);
//        plugin.getServer().broadcast(formattedMessage);

        chatResponder.sendResponse(message, player);
    }

    public String getOriginalMessage(Component message) {
        return plainTextSerializer.serialize(message);
    }
}
