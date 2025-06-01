package me.seetaadev.serverfiller.hooks;

import me.seetaadev.serverfiller.RedisMessage;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.Bot;
import me.seetaadev.serverfiller.hooks.discordsrv.DiscordSRVHook;
import me.seetaadev.serverfiller.hooks.luckperms.LuckPermsHook;
import me.seetaadev.serverfiller.hooks.mmoitems.MMOItemsHook;
import me.seetaadev.serverfiller.hooks.proxy.ProxyHook;
import me.seetaadev.serverfiller.hooks.voting.VotingHook;
import org.bukkit.Bukkit;

public class HookManager {

    private final ServerFillerPlugin plugin;
    private DiscordSRVHook discordSRVHook = null;
    private LuckPermsHook luckPermsHook = null;
    private VotingHook votingHook = null;
    private MMOItemsHook mmoItemsHook = null;
    private ProxyHook proxyHook = null;

    public HookManager(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            discordSRVHook = new DiscordSRVHook();
            plugin.getComponentLogger().info("DiscordSRV Hook enabled");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            luckPermsHook = new LuckPermsHook();
            plugin.getComponentLogger().info("LuckPerms Hook enabled");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("VotingPlugin")) {
            votingHook = new VotingHook(plugin);
            plugin.getComponentLogger().info("VotingPlugin Hook enabled");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            mmoItemsHook = new MMOItemsHook();
            plugin.getComponentLogger().info("MMOItems Hook enabled");
        }

        if (plugin.getBotFactory().botBuilder().isProxyEnabled()) {
            proxyHook = new ProxyHook(plugin);
            proxyHook.connect();
            plugin.getComponentLogger().info("Proxy Hook enabled");
        }
    }

    public void stop() {
        if (plugin.getBotFactory().botBuilder().isProxyEnabled()) {
            //int onlineBots = plugin.getBotFactory().onlineBotsCount();
            //if (onlineBots > 0)

            if (proxyHook != null) {
                proxyHook.disconnect();
            }
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

    public void createData(Bot bot) {
        if (mmoItemsHook != null) {
            mmoItemsHook.createData(bot);
        }
    }

    public void sendProxyMessage(RedisMessage message) {
        if (proxyHook != null) {
            proxyHook.sendMessageToProxy(message);
        }
    }
}
