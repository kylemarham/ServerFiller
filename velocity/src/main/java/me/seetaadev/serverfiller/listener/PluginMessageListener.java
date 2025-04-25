package me.seetaadev.serverfiller.listener;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import me.seetaadev.serverfiller.ServerFillerPlugin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PluginMessageListener implements EventHandler<PluginMessageEvent> {

    private final ServerFillerPlugin plugin;

    public PluginMessageListener(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(PluginMessageEvent event) {
        if (!event.getIdentifier().getId().equalsIgnoreCase("MyChannel")) return;

        byte[] data = event.getData();
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            String received = in.readUTF();
            System.out.println("Mensaje recibido del backend: " + received);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
