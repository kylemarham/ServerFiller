package me.seetaadev.serverfiller.bot.service.actions;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.Bot;
import me.seetaadev.serverfiller.bot.BotFactory;
import me.seetaadev.serverfiller.bot.settings.BotActionSettings;
import me.seetaadev.serverfiller.hooks.HookManager;
import org.bukkit.scheduler.BukkitRunnable;

public class VoteAction implements Action {

    private BukkitRunnable voteTask;
    private final ServerFillerPlugin plugin;
    private final BotFactory botFactory;
    private final BotActionSettings config;
    private final HookManager hook;

    public VoteAction(ServerFillerPlugin plugin, BotActionSettings config) {
        this.plugin = plugin;
        this.botFactory = plugin.getBotFactory();
        this.config = config;
        this.hook = plugin.getHookManager();
    }

    @Override
    public void start() {
        if (config.isVotingEnabled()) {
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
        voteTask.runTaskLater(plugin, getRandomDelayTicks(config.getMinTimeBetweenVotes(), config.getMaxTimeBetweenVotes()));
    }

    @Override
    public void stop() {
        if (voteTask != null) {
            voteTask.cancel();
            voteTask = null;
        }
    }
}
