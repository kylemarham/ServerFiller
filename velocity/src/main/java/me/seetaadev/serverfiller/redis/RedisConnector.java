package me.seetaadev.serverfiller.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import org.slf4j.Logger;

import java.time.Duration;

public class RedisConnector {

    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;

    private final ServerFillerPlugin plugin;
    private final Logger logger;

    public RedisConnector(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public boolean connect(String host, int port, int timeout, String username, String password) {
        RedisURI.Builder redisURI = RedisURI.builder();

        boolean auth;
        redisURI.withHost(host);
        redisURI.withPort(port);

        redisURI.withTimeout(Duration.ofSeconds(timeout));
        if (username != null) {
            redisURI.withClientName(username);
        }

        if (password != null) {
            CharSequence passwordSequence = password;
            redisURI.withPassword(passwordSequence);
            auth = true;
        } else {
            auth = false;
        }

        try {
            client = RedisClient.create(redisURI.build());
            connection = client.connect();
            pubSubConnection = client.connectPubSub();
            RedisListener listener = new RedisListener(this.plugin);
            pubSubConnection.addListener(listener);
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
            plugin.disable();
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
