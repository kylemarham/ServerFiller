package me.seetaadev.serverfiller.hooks.proxy;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

public class ProxyHook {

    private final ServerFillerPlugin plugin;

    public ProxyHook(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendMessageToProxy(Player player, MessageType messageType) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(out);

        try {
            dataOut.writeUTF("Forward");
            dataOut.writeUTF("ALL");
            dataOut.writeUTF("ServerFillerChannel");

            ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
            DataOutputStream msgOut = new DataOutputStream(msgBytes);
            msgOut.writeUTF(messageType.name());

            byte[] msg = msgBytes.toByteArray();
            dataOut.writeShort(msg.length);
            dataOut.write(msg);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error sending message to proxy", e);
            return;
        }

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
