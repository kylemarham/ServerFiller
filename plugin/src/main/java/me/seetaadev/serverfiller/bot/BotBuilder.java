package me.seetaadev.serverfiller.bot;

import com.google.gson.Gson;
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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String uuid = getUUIDFromMojang(playerName);
                if (uuid != null) {
                    Property skin = getSkinFromMojang(uuid);
                    if (skin != null) {
                        applySkin(profile, skin);
                        return;
                    }
                }

                Property backupSkin = getSkinFromCrafatar(playerName);
                if (backupSkin != null) {
                    applySkin(profile, backupSkin);
                    return;
                }

                plugin.getComponentLogger().warn("No skin could be loaded for '{}'", playerName);
            } catch (Exception e) {
                plugin.getComponentLogger().warn("Exception while loading skin for '{}': {}", playerName, e.getMessage());
            }
        });
    }

    private String getUUIDFromMojang(String playerName) {
        try {
            URI uri = new URI("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            URL url = URL.of(uri, null);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            if (con.getResponseCode() != 200) return null;

            try (Reader reader = new InputStreamReader(con.getInputStream())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                return json.get("id").getAsString();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Property getSkinFromMojang(String uuid) {
        try {
            URI uri = new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            URL url = URL.of(uri, null);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            if (con.getResponseCode() != 200) return null;

            try (Reader reader = new InputStreamReader(con.getInputStream())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                JsonObject property = json.getAsJsonArray("properties").get(0).getAsJsonObject();
                return new Property("textures", property.get("value").getAsString(), property.get("signature").getAsString());
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Property getSkinFromCrafatar(String playerName) {
        try {
            String uuid = getUUIDFromMojang(playerName);
            if (uuid == null) return null;

            String textureJson = new Gson().toJson(Map.of(
                    "textures", Map.of(
                            "SKIN", Map.of(
                                    "url", "https://crafatar.com/skins/" + uuid
                            )
                    )
            ));

            String base64Value = Base64.getEncoder().encodeToString(textureJson.getBytes(StandardCharsets.UTF_8));
            return new Property("textures", base64Value, null);
        } catch (Exception e) {
            plugin.getComponentLogger().warn("Failed to generate skin from Crafatar for '{}': {}", playerName, e.getMessage());
            return null;
        }
    }

    private void applySkin(GameProfile profile, Property skin) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            profile.getProperties().put("textures", skin);
        });
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
