package me.seetaadev.serverfiller.bot.service;

import com.bencodez.votingplugin.VotingPluginHooks;
import com.bencodez.votingplugin.events.PlayerVoteEvent;
import com.bencodez.votingplugin.listeners.VotiferEvent;
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
    private BukkitRunnable task;

    private int minOnlineBots;
    private int maxOnlineBots;
    private int minTimeBetweenActions;
    private int maxTimeBetweenActions;

    public BotActionService(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        this.botFactory = plugin.getBotFactory();
    }

    public void load() {
        FileConfiguration config = new ConfigFile(plugin, null, "config", true).getConfig();
        minOnlineBots = config.getInt("bots.online.min", 25);
        maxOnlineBots = config.getInt("bots.online.max", 50);
        minTimeBetweenActions = config.getInt("bots.timeActions.min", 10);
        maxTimeBetweenActions = config.getInt("bots.timeActions.max", 30);
    }

    public void reload() {
        load();
        start();
    }

    public void start() {
        stop();
        scheduleNextRun();
    }

    private void scheduleNextRun() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                Random rand = new Random();
                int voteOrJoin = rand.nextInt(2);
                if (voteOrJoin == 0) {
                    Bot bot = botFactory.randomOnlineBot(false);
                    if (bot != null && vote(bot)) {
                        scheduleNextRun();
                        return;
                    }
                }

                joinOrLeave();
                scheduleNextRun();
            }
        };

        task.runTaskLater(plugin, getRandomDelayTicks());
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
        VoteSite voteSite = votingPlugin.getMainClass().getVoteSites()
                .get(random.nextInt(votingPlugin.getMainClass().getVoteSites().size()));

        VotingPluginUser user = votingPlugin.getUserManager().getVotingPluginUser(bot.getUniqueId());
        if (!user.canVoteSite(voteSite)) {
            PlayerVoteEvent voteEvent = new PlayerVoteEvent(voteSite, bot.getName(), voteSite.getServiceSite(), true);
            Bukkit.getPluginManager().callEvent(voteEvent);
            return true;
        }

        return false;
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private int getRandomDelayTicks() {
        int secondsRange = maxTimeBetweenActions - minTimeBetweenActions + 1;
        int seconds = minTimeBetweenActions + random.nextInt(Math.max(secondsRange, 1));
        return seconds * 20;
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
                // Determine how many bots we need, then spawn a batch (max 5 per tick)
                int missing = minOnlineBots - currentCount;
                int batchSize = Math.min(missing, 5);
                for (int i = 0; i < batchSize; i++) {
                    Bot bot = botFactory.randomOfflineBot();
                    if (bot != null) {
                        bot.spawn();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
