package me.seetaadev.serverfiller.bot.service.actions;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.Bot;
import me.seetaadev.serverfiller.bot.BotFactory;
import me.seetaadev.serverfiller.bot.service.BotActionService;
import me.seetaadev.serverfiller.hooks.HookManager;
import org.bukkit.scheduler.BukkitRunnable;

public class BuyAction implements Action {

    private BukkitRunnable voteTask;
    private final ServerFillerPlugin plugin;
    private final BotFactory botFactory;
    private final BotActionService botActionService;
    private final HookManager hook;

    public BuyAction(ServerFillerPlugin plugin, BotActionService botActionService) {
        this.plugin = plugin;
        this.botFactory = plugin.getBotFactory();
        this.botActionService = botActionService;
        this.hook = plugin.getHookManager();
    }

    @Override
    public void start() {
        if (botActionService.getConfig().isB) {
            return;
        }

        voteTask = new BukkitRunnable() {
            @Override
            public void run() {
                Bot bot = botFactory.randomOnlineBot(false);
                if (bot != null) {
                    hook.vote(bot);
                }

                start();
            }
        };
        voteTask.runTaskLater(plugin, getRandomDelayTicks(botActionService.getConfig().getMinTimeBetweenVotes(),
                botActionService.getConfig().getMaxTimeBetweenVotes()));
    }

    @Override
    public void stop() {
        if (voteTask != null) {
            voteTask.cancel();
            voteTask = null;
        }
    }
}
