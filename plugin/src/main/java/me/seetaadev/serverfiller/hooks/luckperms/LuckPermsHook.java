package me.seetaadev.serverfiller.hooks.luckperms;

import org.bukkit.Bukkit;

public class LuckPermsHook {

    public void giveRank(String name, String rank) {
        String command = "lp user " + name + " parent set " + rank;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
