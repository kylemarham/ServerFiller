package me.seetaadev.serverfiller.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.config.ConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class RedisConnector {
    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;
    private RedisAsyncCommands<String, String> asyncCommands;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;

    private final ServerFillerPlugin plugin;
    private final Logger logger;

    public RedisConnector(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
    }

    public boolean connect() {
        RedisURI.Builder redisURI = RedisURI.builder();

        FileConfiguration config = new ConfigFile(plugin, null, "redis", true).getConfig();
        boolean auth;
        redisURI.withHost(config.getString("redis.host"));
        redisURI.withPort(config.getInt("redis.port"));

        redisURI.withTimeout(Duration.ofSeconds(config.getInt("redis.timeout")));
        if (config.getString("redis.username") != null && !Objects.requireNonNull(config.getString("redis.username")).isEmpty()) {
            redisURI.withClientName(config.getString("redis.username"));
        }

        if (config.getString("redis.password") != null && !Objects.requireNonNull(config.getString("redis.password")).isEmpty()) {
            CharSequence password = config.getString("redis.password");
            redisURI.withPassword(password);

            auth = true;
        } else {
            auth = false;
        }

        try {
            client = RedisClient.create(redisURI.build());
            connection = client.connect();
            asyncCommands = connection.async();
            pubSubConnection = client.connectPubSub();
            RedisPubSubAsyncCommands<String, String> pubSubAsyncCommands = pubSubConnection.async();
            pubSubAsyncCommands.subscribe("serverfiller").thenAccept(subscribedChannels ->
                    logger.info("Subscribed to Redis channel: serverfiller")).exceptionally(throwable -> {
                logger.error("Failed to subscribe to Redis channel: serverfiller");
                return null;
            });

            logger.info("Connected to Redis {} auth", auth ? "with" : "without");
            return true;
        } catch (Exception e) {
            logger.error("Failed to connect to Redis");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return false;
        }
    }

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
            if (pubSubConnection != null) {
                pubSubConnection.close();
            }
            if (client != null) {
                client.shutdown();
            }

            logger.debug("Disconnected from Redis successfully.");
        } catch (Exception e) {
            logger.error("Failed to disconnect from Redis: {}", e.getMessage());
        }
    }

    public void write(String json) {
        String channel = "serverfiller";
        AtomicBoolean completed = new AtomicBoolean(false);
        try {
            asyncCommands.publish(channel, json).thenAccept(publishResult -> {
                logger.debug("Data written to Redis with channel: {}", channel);
                completed.set(true);
            }).exceptionally(throwable -> {
                completed.set(false);
                return null;
            });
        } catch (Exception e) {
            logger.error("Failed to write to Redis");
            completed.set(false);
        }

        completed.get();
    }
}
