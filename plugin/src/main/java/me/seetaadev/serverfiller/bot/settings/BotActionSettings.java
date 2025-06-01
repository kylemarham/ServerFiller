package me.seetaadev.serverfiller.bot.settings;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.config.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;

public class BotActionSettings {

    private final int minOnlineBots;
    private final int maxOnlineBots;
    private final int minTimeBetweenJoinOrLeave;
    private final int maxTimeBetweenJoinOrLeave;
    private final int minTimeBetweenVotes;
    private final int maxTimeBetweenVotes;
    private final boolean votingEnabled;
    private final int minBuyMessageInterval;
    private final int maxBuyMessageInterval;
    private final boolean buyMessageEnabled;

    public BotActionSettings(ServerFillerPlugin plugin) {
        FileConfiguration config = new ConfigFile(plugin, null, "config", true).getConfig();
        this.minOnlineBots = config.getInt("bots.online.min", 25);
        this.maxOnlineBots = config.getInt("bots.online.max", 50);
        this.minTimeBetweenJoinOrLeave = config.getInt("bots.timeActions.min", 10);
        this.maxTimeBetweenJoinOrLeave = config.getInt("bots.timeActions.max", 30);
        this.minTimeBetweenVotes = config.getInt("bots.timeVotes.min", 10);
        this.maxTimeBetweenVotes = config.getInt("bots.timeVotes.max", 30);
        this.votingEnabled = config.getBoolean("bots.timeVotes.enabled", true);
        this.minBuyMessageInterval = config.getInt("bots.buyMessage.min", 10);
        this.maxBuyMessageInterval = config.getInt("bots.buyMessage.max", 30);
        this.buyMessageEnabled = config.getBoolean("bots.buyMessage.enabled", true);
    }

    public int getMinOnlineBots() {
        return minOnlineBots;
    }

    public int getMaxOnlineBots() {
        return maxOnlineBots;
    }

    public int getMinTimeBetweenJoinOrLeave() {
        return minTimeBetweenJoinOrLeave;
    }

    public int getMaxTimeBetweenJoinOrLeave() {
        return maxTimeBetweenJoinOrLeave;
    }

    public int getMinTimeBetweenVotes() {
        return minTimeBetweenVotes;
    }

    public int getMaxTimeBetweenVotes() {
        return maxTimeBetweenVotes;
    }

    public boolean isVotingEnabled() {
        return votingEnabled;
    }

    public int getMinBuyMessageInterval() {
        return minBuyMessageInterval;
    }

    public int getMaxBuyMessageInterval() {
        return maxBuyMessageInterval;
    }

    public boolean isBuyMessageEnabled() {
        return buyMessageEnabled;
    }
}
