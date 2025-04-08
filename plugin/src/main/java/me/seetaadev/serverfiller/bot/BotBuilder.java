package me.seetaadev.serverfiller.bot;

import com.mojang.authlib.GameProfile;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.settings.BotSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

public class BotBuilder {

    private ServerFillerPlugin plugin;
    private String hostname;
    private int port;

    public Bot createBot(BotSettings settings) {
        ServerLevel world = ((CraftWorld) Bukkit.getWorlds().getFirst()).getHandle();
        GameProfile profile = new GameProfile(settings.getUUID(), settings.getName());

        ServerPlayer serverPlayer = new ServerPlayer(Bot.SERVER, world, profile, Bot.CLIENT_INFORMATION);
        Bot bot = new Bot((CraftServer) Bukkit.getServer(), serverPlayer, settings, hostname, port, plugin);
        return bot.saveBot();
    }

    public void setPlugin(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
