package me.seetaadev.serverfiller.hooks;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.Bot;
import me.seetaadev.serverfiller.hooks.discordsrv.DiscordSRVHook;
import me.seetaadev.serverfiller.hooks.luckperms.LuckPermsHook;
import me.seetaadev.serverfiller.hooks.voting.VotingHook;
import org.bukkit.Bukkit;

public class HookManager {

    private final ServerFillerPlugin plugin;
    private DiscordSRVHook discordSRVHook;
    private LuckPermsHook luckPermsHook;
    private VotingHook votingHook;

    public HookManager(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            discordSRVHook = new DiscordSRVHook();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            luckPermsHook = new LuckPermsHook();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("VotingPlugin")) {
            votingHook = new VotingHook(plugin);
        }
    }

    public void sendDiscordMessage(String message, Bot bot) {
        if (discordSRVHook != null) {
            discordSRVHook.sendDiscordMessage(message, bot);
        }
    }

    public void giveRank(String name, String rank) {
        if (luckPermsHook != null) {
            luckPermsHook.giveRank(name, rank);
        }
    }

    public void vote(Bot bot) {
        votingHook.vote(bot);
    }
}
