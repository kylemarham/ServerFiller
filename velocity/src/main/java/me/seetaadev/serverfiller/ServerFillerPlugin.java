package me.seetaadev.serverfiller;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import me.seetaadev.serverfiller.listener.PlayerJoinListener;
import me.seetaadev.serverfiller.listener.PlayerQuitListener;
import me.seetaadev.serverfiller.listener.ProxyPingListener;
import org.slf4j.Logger;


@Plugin(id = "serverfiller", name = "ServerFiller", version = "1.2",
       description = "Create bots with ia", authors = {"SeeTaaDev"})
public class ServerFillerPlugin {

    private int normalPlayers;
    private int fakePlayers;

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public ServerFillerPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.info("Hello there! I made my first plugin with Velocity.");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getEventManager().register(this, ProxyPingEvent.class, new ProxyPingListener(this));
        server.getEventManager().register(this, PostLoginEvent.class, new PlayerJoinListener(this));
        server.getEventManager().register(this, DisconnectEvent.class, new PlayerQuitListener(this));
        logger.info("Initialized ServerFiller plugin.");
    }

    public void addNormalPlayers() {
        this.normalPlayers += 1;
    }

    public void addFakePlayers() {
        this.fakePlayers += 1;
    }

    public int getNormalPlayers() {
        return normalPlayers;
    }

    public int getFakePlayers() {
        return fakePlayers;
    }

    public void removeNormalPlayers() {
        this.normalPlayers -= 1;
    }

    public void removeFakePlayers() {
        this.fakePlayers -= 1;
    }
}
