package me.seetaadev.serverfiller.bot;

import com.mojang.authlib.GameProfile;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.service.BotSkinService;
import me.seetaadev.serverfiller.bot.settings.BotSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

public class BotBuilder {

    private String hostname;
    private int port;
    private boolean skinsEnabled;
    private boolean shouldWeWelcomeBots;
    private boolean chatWelcomeEnabled;
    private double welcomeChance;
    private int welcomeMin;
    private int welcomeMax;
    private boolean proxyEnabled;

    private final ServerFillerPlugin plugin;
    private final BotSkinService botSkinService;

    public BotBuilder(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        botSkinService = new BotSkinService(plugin);
    }

    public Bot createBot(BotSettings settings) {
        ServerLevel world = ((CraftWorld) Bukkit.getWorlds().getFirst()).getHandle();
        GameProfile profile = new GameProfile(settings.getUUID(), settings.getName());
        if (skinsEnabled) botSkinService.loadSkin(profile, settings.getName());

        ServerPlayer serverPlayer = new ServerPlayer(Bot.SERVER, world, profile, Bot.CLIENT_INFORMATION);
        Bot bot = new Bot((CraftServer) Bukkit.getServer(), serverPlayer, settings, hostname, port, plugin);
        return bot.saveBot();
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSkinsEnabled(boolean skinsEnabled) {
        this.skinsEnabled = skinsEnabled;
    }

    public void setShouldWeWelcomeBots(boolean shouldWeWelcomeBots) {
        this.shouldWeWelcomeBots = shouldWeWelcomeBots;
    }

    public boolean isShouldWeWelcomeBots() {
        return shouldWeWelcomeBots;
    }

    public void setChatWelcomeEnabled(boolean chatWelcomeEnabled) {
        this.chatWelcomeEnabled = chatWelcomeEnabled;
    }

    public boolean isChatWelcomeEnabled() {
        return chatWelcomeEnabled;
    }

    public void setWelcomeChance(double welcomeChance) {
        this.welcomeChance = welcomeChance;
    }

    public double getWelcomeChance() {
        return welcomeChance;
    }

    public void setWelcomeMin(int welcomeMin) {
        this.welcomeMin = welcomeMin;
    }

    public int getWelcomeMin() {
        return welcomeMin;
    }

    public void setWelcomeMax(int welcomeMax) {
        this.welcomeMax = welcomeMax;
    }

    public int getWelcomeMax() {
        return welcomeMax;
    }

    public void setProxyEnabled(boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }
}
