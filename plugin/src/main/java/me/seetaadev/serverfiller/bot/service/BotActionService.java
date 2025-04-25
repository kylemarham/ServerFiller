package me.seetaadev.serverfiller.bot.service;

import com.bencodez.votingplugin.VotingPluginHooks;
import com.bencodez.votingplugin.events.PlayerVoteEvent;
import com.bencodez.votingplugin.objects.VoteSite;
import com.bencodez.votingplugin.user.VotingPluginUser;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.Bot;
import me.seetaadev.serverfiller.bot.BotFactory;
import me.seetaadev.serverfiller.bot.settings.BotActionSettings;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class BotActionService {

    private final ServerFillerPlugin plugin;
    private final BotFactory botFactory;
    private final Random random = new Random();

    private BukkitRunnable voteTask;
    private BukkitRunnable joinLeaveTask;

    private BotActionSettings config;

    public BotActionService(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        this.botFactory = plugin.getBotFactory();
    }

    public void load() {
        this.config = new BotActionSettings(plugin);
    }

    public void reload() {
        load();
        start();
    }

    public void start() {
        stop();
        scheduleJoinLeaveTask();
        if (config.isVotingEnabled()) {
            scheduleVoteTask();
        }
    }

    public void stop() {
        if (voteTask != null) {
            voteTask.cancel();
            voteTask = null;
        }
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
                if (current >= config.getMinOnlineBots()) {
                    cancel();
                    return;
                }

                int missing = config.getMinOnlineBots() - current;
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

    private void scheduleJoinLeaveTask() {
        joinLeaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                joinOrLeave();
                scheduleJoinLeaveTask();
            }
        };
        joinLeaveTask.runTaskLater(plugin, getRandomDelayTicks(config.getMinTimeBetweenJoinOrLeave(), config.getMaxTimeBetweenJoinOrLeave()));
    }

    private void scheduleVoteTask() {
        voteTask = new BukkitRunnable() {
            @Override
            public void run() {
                Bot bot = botFactory.randomOnlineBot(false);
                if (bot != null) {
                    vote(bot);
                }
                scheduleVoteTask();
            }
        };
        voteTask.runTaskLater(plugin, getRandomDelayTicks(config.getMinTimeBetweenVotes(), config.getMaxTimeBetweenVotes()));
    }

    private void joinOrLeave() {
        int currentCount = botFactory.onlineBotsCount();
        Bot bot;

        boolean shouldJoin = currentCount < config.getMinOnlineBots() ||
                (currentCount <= config.getMaxOnlineBots() && random.nextBoolean());

        if (shouldJoin) {
            bot = botFactory.randomOfflineBot();
            if (bot != null) bot.spawn();
        } else {
            bot = botFactory.randomOnlineBot(true);
            if (bot != null) bot.despawn();
        }
    }

    private void vote(Bot bot) {
        VotingPluginHooks votingPlugin = VotingPluginHooks.getInstance();
        List<VoteSite> voteSites = votingPlugin.getMainClass().getVoteSites();

        if (voteSites.isEmpty()) return;

        VoteSite voteSite = voteSites.get(random.nextInt(voteSites.size()));
        if (voteSite == null) return;

        VotingPluginUser user = votingPlugin.getUserManager().getVotingPluginUser(bot.getUniqueId());
        if (!user.canVoteSite(voteSite)) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerVoteEvent event = new PlayerVoteEvent(
                    voteSite,
                    bot.getName(),
                    voteSite.getServiceSite(),
                    true
            );
            Bukkit.getPluginManager().callEvent(event);
        });
    }

    private int getRandomDelayTicks(int min, int max) {
        int range = Math.max(max - min + 1, 1);
        return (min + random.nextInt(range)) * 20;
    }
}
