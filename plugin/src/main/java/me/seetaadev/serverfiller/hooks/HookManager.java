package me.seetaadev.serverfiller.hooks;

import me.seetaadev.serverfiller.bot.Bot;
import me.seetaadev.serverfiller.hooks.discordsrv.DiscordSRVHook;
import me.seetaadev.serverfiller.hooks.luckperms.LuckPermsHook;
import org.bukkit.Bukkit;

public class HookManager {

    private DiscordSRVHook discordSRVHook;
    private LuckPermsHook luckPermsHook;

    public void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            discordSRVHook = new DiscordSRVHook();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            luckPermsHook = new LuckPermsHook();
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
}
