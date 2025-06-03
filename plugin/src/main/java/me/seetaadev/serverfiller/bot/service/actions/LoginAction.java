package me.seetaadev.serverfiller.bot.service.actions;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.Bot;
import me.seetaadev.serverfiller.bot.BotFactory;
import me.seetaadev.serverfiller.bot.service.BotActionService;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class LoginAction implements Action {

    private BukkitRunnable joinLeaveTask;
    private final ServerFillerPlugin plugin;
    private final BotFactory botFactory;
    private final BotActionService botActionService;

    public LoginAction(ServerFillerPlugin plugin, BotActionService botActionService) {
        this.plugin = plugin;
        this.botFactory = plugin.getBotFactory();
        this.botActionService = botActionService;
    }

    @Override
    public void start() {
        joinLeaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                joinOrLeave();
                start();
            }
        };
        joinLeaveTask.runTaskLater(plugin, getRandomDelayTicks(botActionService.getConfig().getMinTimeBetweenJoinOrLeave(),
                botActionService.getConfig().getMaxTimeBetweenJoinOrLeave()));
    }

    private void joinOrLeave() {
        int currentCount = botFactory.onlineBotsCount();
        Bot bot;

        boolean shouldJoin = currentCount < botActionService.getConfig().getMinOnlineBots() ||
                (currentCount <= botActionService.getConfig().getMaxOnlineBots() && rand.nextBoolean());

        if (shouldJoin) {
            bot = botFactory.randomOfflineBot();
            if (bot != null) bot.spawn();
        } else {
            bot = botFactory.randomOnlineBot(true);
            if (bot != null) bot.despawn();
        }
    }

    @Override
    public void stop() {
        if (joinLeaveTask != null) {
            joinLeaveTask.cancel();
            joinLeaveTask = null;
        }
    }

    public void ensureMinimum() {
        new BukkitRunnable() {
            @Override
            public void run() {
                int current = botFactory.onlineBotsCount();
                if (current >= botActionService.getConfig().getMinOnlineBots()) {
                    cancel();
                    return;
                }

                int missing = botActionService.getConfig().getMinOnlineBots() - current;
                int batchSize = Math.min(missing, 5);

                for (int i = 0; i < batchSize; i++) {
                    Bot bot = botFactory.randomOfflineBot();
                    if (bot != null) {
                        Bukkit.getScheduler().runTaskLater(plugin, bot::spawn, i * 10L);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
