package me.seetaadev.serverfiller.bot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.settings.BotSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class BotBuilder {

    private ServerFillerPlugin plugin;
    private String hostname;
    private int port;

    public Bot createBot(BotSettings settings) {
        ServerLevel world = ((CraftWorld) Bukkit.getWorlds().getFirst()).getHandle();
        GameProfile profile = new GameProfile(settings.getUUID(), settings.getName());
        loadSkin(profile, settings.getName());

        ServerPlayer serverPlayer = new ServerPlayer(Bot.SERVER, world, profile, Bot.CLIENT_INFORMATION);
        Bot bot = new Bot((CraftServer) Bukkit.getServer(), serverPlayer, settings, hostname, port, plugin);
        return bot.saveBot();
    }

    public void loadSkin(GameProfile profile, String playerName) {
        try {
            URI uuidUri = URI.create("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            URL uuidUrl = URL.of(uuidUri, null);
            HttpURLConnection uuidConnection = (HttpURLConnection) uuidUrl.openConnection();
            uuidConnection.setReadTimeout(5000);
            uuidConnection.setConnectTimeout(5000);

            if (uuidConnection.getResponseCode() != 200) return;

            try (Reader uuidReader = new InputStreamReader(uuidConnection.getInputStream())) {
                JsonObject uuidJson = JsonParser.parseReader(uuidReader).getAsJsonObject();
                String uuid = uuidJson.get("id").getAsString();

                URI sessionUri = URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
                URL sessionUrl = URL.of(sessionUri, null);
                HttpURLConnection sessionConnection = (HttpURLConnection) sessionUrl.openConnection();
                sessionConnection.setReadTimeout(5000);
                sessionConnection.setConnectTimeout(5000);

                if (sessionConnection.getResponseCode() != 200) return;

                try (Reader sessionReader = new InputStreamReader(sessionConnection.getInputStream())) {
                    JsonObject sessionJson = JsonParser.parseReader(sessionReader).getAsJsonObject();
                    JsonObject property = sessionJson.getAsJsonArray("properties").get(0).getAsJsonObject();

                    String value = property.get("value").getAsString();
                    String signature = property.get("signature").getAsString();

                    profile.getProperties().put("textures", new Property("textures", value, signature));
                }
            }
        } catch (Exception ignored) {
            plugin.getComponentLogger().warn("Failed to load skin for player: " + playerName);
        }
    }

    public void setPlugin(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
