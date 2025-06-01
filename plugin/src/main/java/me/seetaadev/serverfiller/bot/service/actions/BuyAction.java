package me.seetaadev.serverfiller.bot.service.actions;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.Bot;
import me.seetaadev.serverfiller.bot.BotFactory;
import me.seetaadev.serverfiller.bot.service.BotActionService;
import me.seetaadev.serverfiller.bot.settings.ItemSettings;
import net.kyori.adventure.text.Component;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;

public class BuyAction implements Action {

    private BukkitRunnable buyTask;
    private final ServerFillerPlugin plugin;
    private final BotFactory botFactory;
    private final BotActionService botActionService;

    public BuyAction(ServerFillerPlugin plugin, BotActionService botActionService) {
        this.plugin = plugin;
        this.botFactory = plugin.getBotFactory();
        this.botActionService = botActionService;
    }

    @Override
    public void start() {
        if (!botActionService.getConfig().isBuyMessageEnabled()) return;

        buyTask = new BukkitRunnable() {
            @Override
            public void run() {
                Bot bot = botFactory.randomOnlineBot(false);
                if (bot != null) {
                    sendMessage(bot);
                }

                start();
            }
        };
        buyTask.runTaskLater(plugin, getRandomDelayTicks(botActionService.getConfig().getMinBuyMessageInterval(),
                botActionService.getConfig().getMaxBuyMessageInterval()));
    }

    public void sendMessage(Bot bot) {
        ItemSettings.BuyItem buyItem = botActionService.getItemSettings().randomBuyItem();
        if (buyItem == null) return;

        List<Component> messages = plugin.getMessageHandler().formatList("buy", Map.of("{player_name}", bot.getName(),
                "{item_name}", buyItem.itemName()));

        messages.forEach(bot::sendMessage);
    }

    @Override
    public void stop() {
        if (buyTask != null) {
            buyTask.cancel();
            buyTask = null;
        }
    }
}
