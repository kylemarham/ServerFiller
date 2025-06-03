package me.seetaadev.serverfiller.redis;

import com.google.gson.Gson;
import io.lettuce.core.pubsub.RedisPubSubListener;
import me.seetaadev.serverfiller.RedisMessage;
import me.seetaadev.serverfiller.ServerFillerPlugin;

public class RedisListener implements RedisPubSubListener<String, String> {

    private final ServerFillerPlugin plugin;

    public RedisListener(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void message(String channel, String message) {
        if (!channel.equals("serverfiller")) return;

        RedisMessage redisMessage = new Gson().fromJson(message, RedisMessage.class);
        switch (redisMessage.getPayload()) {
            case ADD:
                plugin.addFakePlayers();
                break;
            case REMOVE:
                plugin.removeFakePlayers();
                break;
            case REMOVE_ALL:
                plugin.removeFakePlayers(Integer.parseInt(redisMessage.getParam("amount")));
                break;
        }
    }

    @Override
    public void message(String pattern, String channel, String message) {
        message(channel, message);
    }

    @Override
    public void subscribed(String channel, long count) {
        plugin.getLogger().debug("Subscribed to channel: {}", channel);
    }

    @Override
    public void psubscribed(String pattern, long count) {
        plugin.getLogger().debug("Pattern subscribed: {}", pattern);
    }

    @Override
    public void unsubscribed(String channel, long count) {
        plugin.getLogger().debug("Unsubscribed from channel: {}", channel);
    }

    @Override
    public void punsubscribed(String pattern, long count) {
        plugin.getLogger().debug("Pattern unsubscribed: {}", pattern);
    }
}
