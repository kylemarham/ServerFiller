package me.seetaadev.serverfiller.hooks.proxy;

import me.seetaadev.serverfiller.RedisMessage;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.redis.RedisConnector;

public class ProxyHook {

    private final RedisConnector connector;

    public ProxyHook(ServerFillerPlugin plugin) {
        this.connector = new RedisConnector(plugin);
    }

    public void connect() {
        connector.connect();
    }

    public void disconnect() {
        connector.disconnect();
    }

    public void sendMessageToProxy(RedisMessage message) {
        connector.write(message.toJSON());
    }
}
