package me.seetaadev.serverfiller.listener;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import me.seetaadev.serverfiller.ServerFillerPlugin;

public class ProxyPingListener implements EventHandler<ProxyPingEvent> {

    private final ServerFillerPlugin plugin;

    public ProxyPingListener(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ProxyPingEvent event) {
        int currentPlayers = plugin.getNormalPlayers() + plugin.getFakePlayers();
        ServerPing ping = event.getPing();
        ServerPing.Builder builder = ping.asBuilder();
        builder.onlinePlayers(currentPlayers);
        event.setPing(builder.notModCompatible()
                .build());
    }
}
