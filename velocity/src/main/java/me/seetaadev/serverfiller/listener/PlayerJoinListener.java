package me.seetaadev.serverfiller.listener;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import me.seetaadev.serverfiller.ServerFillerPlugin;

public class PlayerJoinListener implements EventHandler<PostLoginEvent> {

    private final ServerFillerPlugin plugin;

    public PlayerJoinListener(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(PostLoginEvent event) {
        plugin.addNormalPlayers();
    }
}
