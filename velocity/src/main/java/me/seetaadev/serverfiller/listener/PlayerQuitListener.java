package me.seetaadev.serverfiller.listener;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import me.seetaadev.serverfiller.ServerFillerPlugin;

public class PlayerQuitListener implements EventHandler<DisconnectEvent> {

    private final ServerFillerPlugin plugin;

    public PlayerQuitListener(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(DisconnectEvent event) {
        plugin.removeNormalPlayers();
    }
}
