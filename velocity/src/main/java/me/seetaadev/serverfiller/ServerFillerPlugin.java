package me.seetaadev.serverfiller;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.seetaadev.serverfiller.listener.PlayerJoinListener;
import me.seetaadev.serverfiller.listener.PlayerQuitListener;
import me.seetaadev.serverfiller.listener.ProxyPingListener;
import me.seetaadev.serverfiller.redis.RedisConnector;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;


@Plugin(id = "serverfiller", name = "ServerFiller", version = "1.2",
       description = "Create bots with ia", authors = {"SeeTaaDev"})
public class ServerFillerPlugin {

    private int normalPlayers;
    private int fakePlayers;

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private File redisFile;
    private RedisConnector redisConnector;

    @Inject
    public ServerFillerPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getEventManager().register(this, ProxyPingEvent.class, new ProxyPingListener(this));
        server.getEventManager().register(this, PostLoginEvent.class, new PlayerJoinListener(this));
        server.getEventManager().register(this, DisconnectEvent.class, new PlayerQuitListener(this));
        logger.info("Initialized ServerFiller plugin.");
        generateConfig();

        redisConnector = new RedisConnector(this);
        Toml toml = new Toml().read(redisFile);
        String host = toml.getString("hostname", "localhost");
        int port = toml.getLong("port", 6379L).intValue();
        int timeout = toml.getLong("timeout", 10L).intValue();
        String username = toml.getString("username", "");
        String password = toml.getString("password", "");
        if (username == null || username.isEmpty()) username = null;
        if (password == null || password.isEmpty()) password = null;

        if (redisConnector.connect(host, port, timeout, username, password)) {
            logger.info("Connected to Redis server successfully.");
        } else {
            disable();
            logger.error("Failed to connect to Redis server. Disabling ServerFiller plugin.");
        }
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        redisConnector.disconnect();
    }

    public void generateConfig() {
        File mainDirectory = dataDirectory.toFile();
        if (!mainDirectory.exists() && !mainDirectory.mkdirs()) {
            logger.error("Failed to create data directory: {}", mainDirectory.getAbsolutePath());
            return;
        }

        redisFile = new File(dataDirectory.toFile(), "redis.toml");

        if (!redisFile.exists()) {
            try {
                if (redisFile.createNewFile()) {
                    String defaultConfig = """
                    hostname = "localhost"
                    port = 6379
                    timeout = 10
                    username = ""
                    password = ""
                """;
                    try (FileWriter writer = new FileWriter(redisFile)) {
                        writer.write(defaultConfig);
                        logger.info("Config file created and default content written to: {}", redisFile.getAbsolutePath());
                    }
                } else {
                    logger.error("Failed to create config file at: {}", redisFile.getAbsolutePath());
                }
            } catch (IOException e) {
                logger.error("Error creating or writing to the config file", e);
            }
        } else {
            logger.info("Config file already exists, skipping creation.");
        }
    }

    public void disable() {
        logger.info("Disabling ServerFiller plugin.");
        server.getEventManager().unregisterListeners(this);
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

    public void removeFakePlayers(int count) {
        int totalFakePlayers = this.fakePlayers - count;
        if (totalFakePlayers < 0) {
            totalFakePlayers = 0;
        }
        this.fakePlayers = totalFakePlayers;
    }

    public Logger getLogger() {
        return logger;
    }
}
