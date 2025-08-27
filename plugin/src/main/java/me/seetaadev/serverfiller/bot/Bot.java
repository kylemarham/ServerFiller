package me.seetaadev.serverfiller.bot;

import me.seetaadev.serverfiller.Payload;
import me.seetaadev.serverfiller.RedisMessage;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.connection.ConnectionFactory;
import me.seetaadev.serverfiller.bot.cookie.BotCookieFactory;
import me.seetaadev.serverfiller.bot.responses.ai.AIChatResponder;
import me.seetaadev.serverfiller.bot.service.BotConfigService;
import me.seetaadev.serverfiller.bot.settings.BotSettings;
import net.kyori.adventure.text.Component;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Bot extends CraftPlayer {

    private Connection connection;
    private final BotSettings settings;
    private boolean spawned;
    private final BotConfigService configService;
    private final String hostname;
    private final int port;
    private final ServerFillerPlugin plugin;
    private boolean inCooldown;
    private boolean processingResponse;

    public static final MinecraftServer SERVER = ((CraftServer) Bukkit.getServer()).getServer();
    public static final ClientInformation CLIENT_INFORMATION = ClientInformation.createDefault();


    public Bot(CraftServer craftServer, ServerPlayer entityPlayer, BotSettings settings, String hostname, int port, ServerFillerPlugin plugin) {
        super(craftServer, entityPlayer);
        this.settings = settings;
        this.hostname = hostname;
        this.port = port;
        this.plugin = plugin;
        this.connection = new ConnectionFactory(hostname, port).createConnection(getName(), getUniqueId());
        CommonListenerCookie cookie = BotCookieFactory.make(entityPlayer.getGameProfile(), calculateLatency(), CLIENT_INFORMATION);
        entityPlayer.connection = new ServerGamePacketListenerImpl(SERVER, connection, entityPlayer, cookie);
        this.configService = new BotConfigService(plugin, settings);
        this.spawned = false;
        this.inCooldown = false;
        this.processingResponse = false;
    }

    public void spawn() {
        if (spawned || existsPlayerWithName(settings.getName())) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!getHandle().connection.connection.isConnected()) {
                this.connection = new ConnectionFactory(hostname, port).createConnection(getName(), getUniqueId());
                CommonListenerCookie cookie = BotCookieFactory.make(getHandle().getGameProfile(), calculateLatency(), CLIENT_INFORMATION);
                ServerPlayer newServerPlayer = new ServerPlayer(Bot.SERVER, getHandle().level(), getHandle().getGameProfile(), Bot.CLIENT_INFORMATION);
                newServerPlayer.connection = new ServerGamePacketListenerImpl(Bot.SERVER, connection, getHandle(), cookie);
                this.entity = newServerPlayer;
            }

            plugin.getBotExecutor().submit(() -> {
                AsyncPlayerPreLoginEvent event = new AsyncPlayerPreLoginEvent(getName(),
                        Objects.requireNonNull(getAddress()).getAddress(), getUniqueId(), false);
                Bukkit.getPluginManager().callEvent(event);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bot.SERVER.getPlayerList().placeNewPlayer(connection, getHandle(), BotCookieFactory.make(getProfile(), calculateLatency(), Bot.CLIENT_INFORMATION));
                    Bot.SERVER.getPlayerList().sendAllPlayerInfo(getHandle());
                    spawned = true;

                    if (!settings.hasPlayedBefore()) {
                        plugin.getHookManager().giveRank(getName(), settings.getRank());
                        plugin.getHookManager().createData(this);
                        settings.changePlayedBefore(true);
                        configService.save(settings);
                    }

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Location target = new Location(Bukkit.getWorld("world"), -410, 32, 313);
                        teleport(target);
                        plugin.getHookManager().sendProxyMessage(new RedisMessage(Payload.ADD));
                    }, 120L);
                });
            });
        }, 1L);
    }

    public void despawn() {
        if (!spawned || processingResponse) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            getHandle().connection.disconnect(Component.text("Disconnected"), PlayerKickEvent.Cause.PLUGIN);
            this.spawned = false;
            plugin.getHookManager().sendProxyMessage(new RedisMessage(Payload.REMOVE));
        }, 1L);
    }

    public Bot saveBot() {
        configService.save(settings);
        return this;
    }

    public int calculateLatency() {
        return ThreadLocalRandom.current().nextInt(10, 200);
    }

    public void startCooldown() {
        this.inCooldown = true;
        AIChatResponder AIChatResponder = plugin.getChatResponder().getAIChatResponder();
        int delaySeconds = ThreadLocalRandom.current().nextInt(AIChatResponder.getMinDelay(), AIChatResponder.getMaxDelay() + 1);

        Bukkit.getScheduler().runTaskLater(plugin, () -> this.inCooldown = false, 20L * delaySeconds);
    }

    public boolean existsPlayerWithName(String name) {
        return Bukkit.getPlayer(name) != null;
    }

    public boolean isSpawned() {
        return spawned;
    }

    public String randomWelcomeMessage() {
        return plugin.getBotMessageService().randomWelcomeMessage();
    }

    public String randomFirstJoinMessage() {
        return plugin.getBotMessageService().randomFirstJoinMessage();
    }

    public BotSettings getSettings() {
        return settings;
    }

    public boolean isInCooldown() {
        return inCooldown;
    }

    public void processResponse() {
        this.processingResponse = !processingResponse;
    }
}

