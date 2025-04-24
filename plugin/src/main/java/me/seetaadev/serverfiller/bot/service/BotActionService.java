package me.seetaadev.serverfiller.bot.service;

import com.bencodez.votingplugin.VotingPluginHooks;
import com.bencodez.votingplugin.events.PlayerVoteEvent;
import com.bencodez.votingplugin.objects.VoteSite;
import com.bencodez.votingplugin.user.VotingPluginUser;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.Bot;
import me.seetaadev.serverfiller.bot.BotFactory;
import me.seetaadev.serverfiller.config.ConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class BotActionService {

    private final ServerFillerPlugin plugin;
    private final BotFactory botFactory;
    private final Random random = new Random();

    private BukkitRunnable voteTask;
    private BukkitRunnable joinLeaveTask;

    private int minOnlineBots;
    private int maxOnlineBots;
    private int minTimeBetweenJoinOrLeave;
    private int maxTimeBetweenJoinOrLeave;
    private int minTimeBetweenVotes;
    private int maxTimeBetweenVotes;
    private boolean votingEnabled;

    public BotActionService(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        this.botFactory = plugin.getBotFactory();
    }

    public void load() {
        FileConfiguration config = new ConfigFile(plugin, null, "config", true).getConfig();
        minOnlineBots = config.getInt("bots.online.min", 25);
        maxOnlineBots = config.getInt("bots.online.max", 50);

        minTimeBetweenJoinOrLeave = config.getInt("bots.timeActions.min", 10);
        maxTimeBetweenJoinOrLeave = config.getInt("bots.timeActions.max", 30);

        minTimeBetweenVotes = config.getInt("bots.timeVotes.min", 10);
        maxTimeBetweenVotes = config.getInt("bots.timeVotes.max", 30);
        votingEnabled = config.getBoolean("bots.votingEnabled", true);
    }

    public void reload() {
        load();
        start();
    }

    public void start() {
        stop();
        scheduleJoinLeaveTask();
        if(votingEnabled) {
            scheduleVoteTask();
        }
    }

    private void scheduleVoteTask() {
        voteTask = new BukkitRunnable() {
            @Override
            public void run() {
                Bot bot = botFactory.randomOnlineBot(false);
                if (bot != null) {
                    vote(bot);
                }
                scheduleVoteTask(); // Schedule next vote regardless of result
            }
        };
        voteTask.runTaskLater(plugin, getRandomDelayTicksVote());
    }

    private void scheduleJoinLeaveTask() {
        joinLeaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                joinOrLeave();
                scheduleJoinLeaveTask(); // Always schedule next round
            }
        };
        joinLeaveTask.runTaskLater(plugin, getRandomDelayTicksJoinOrLeave());
    }

    public void joinOrLeave() {
        int currentCount = botFactory.onlineBotsCount();

        Bot bot;
        if (currentCount < minOnlineBots || (currentCount <= maxOnlineBots && random.nextBoolean())) {
            bot = botFactory.randomOfflineBot();
            if (bot != null)
                bot.spawn();

        } else if (currentCount > maxOnlineBots || currentCount >= minOnlineBots) {
            bot = botFactory.randomOnlineBot(true);
            if (bot != null) bot.despawn();
        }
    }

    public boolean vote(Bot bot) {
        VotingPluginHooks votingPlugin = VotingPluginHooks.getInstance();
        Random random = new Random();

        int size = votingPlugin.getMainClass().getVoteSites().size();
        if (size == 0) {
            return false;
        }

        VoteSite voteSite = votingPlugin.getMainClass().getVoteSites().get(random.nextInt(size));
        if (voteSite == null) {
            return false;
        }

        VotingPluginUser user = votingPlugin.getUserManager().getVotingPluginUser(bot.getUniqueId());
        if (user.canVoteSite(voteSite)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                PlayerVoteEvent voteEvent = new PlayerVoteEvent(
                        voteSite,
                        bot.getName(),
                        voteSite.getServiceSite(),
                        true
                );
                Bukkit.getPluginManager().callEvent(voteEvent);
            });
            return true;
        }

        return false;
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

    private int getRandomDelayTicksJoinOrLeave() {
        int secondsRange = maxTimeBetweenJoinOrLeave - minTimeBetweenJoinOrLeave + 1;
        int seconds = minTimeBetweenJoinOrLeave + random.nextInt(Math.max(secondsRange, 1));
        return seconds * 20;
    }

    private int getRandomDelayTicksVote() {
        int secondsRange = maxTimeBetweenVotes - minTimeBetweenVotes + 1;
        int seconds = minTimeBetweenVotes + random.nextInt(Math.max(secondsRange, 1));
        return seconds * 20;
    }

    private int getRandomVoteDelayTicks() {
        // e.g., faster or slower than getRandomDelayTicks()
        return getRandomDelayTicksVote();
    }

    private int getRandomJoinLeaveDelayTicks() {
        return getRandomDelayTicksJoinOrLeave();
    }

    public void ensureMinimum() {
        new BukkitRunnable() {
            @Override
            public void run() {
                int currentCount = botFactory.onlineBotsCount();
                if (currentCount >= minOnlineBots) {
                    cancel();
                    return;
                }

                int missing = minOnlineBots - currentCount;
                int batchSize = Math.min(missing, 5);

                for (int i = 0; i < batchSize; i++) {
                    Bot bot = botFactory.randomOfflineBot();
                    if (bot != null) {
                        int delay = i * 10;
                        Bukkit.getScheduler().runTaskLater(plugin, bot::spawn, delay);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
