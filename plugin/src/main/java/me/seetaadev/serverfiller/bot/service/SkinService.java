package me.seetaadev.serverfiller.bot.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import org.bukkit.Bukkit;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class SkinService {

    private final ServerFillerPlugin plugin;

    public SkinService(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadSkin(GameProfile profile, String playerName) {
        plugin.getBotExecutor().submit(() -> {
            try {
                String uuid = getUUID(playerName);
                Property skin = (uuid != null) ? getSkinFromMojang(uuid) : null;

                if (skin == null) skin = getSkinFromCrafatar(playerName);
                if (skin == null) {
                    plugin.getComponentLogger().warn("No skin found for '{}'", playerName);
                    return;
                }

                applySkin(profile, skin);

            } catch (Exception e) {
                plugin.getComponentLogger().warn("Error loading skin for '{}': {}", playerName, e.getMessage());
            }
        });
    }

    private String getUUID(String playerName) {
        try {
            JsonObject json = getJsonFromUrl("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            return json.get("id").getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private Property getSkinFromMojang(String uuid) {
        try {
            JsonObject json = getJsonFromUrl("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            JsonObject property = json.getAsJsonArray("properties").get(0).getAsJsonObject();
            return new Property("textures", property.get("value").getAsString(), property.get("signature").getAsString());
        } catch (Exception e) {
            return null;
        }
    }

    private Property getSkinFromCrafatar(String playerName) {
        try {
            String uuid = getUUID(playerName);
            if (uuid == null) return null;

            String json = new Gson().toJson(Map.of("textures", Map.of("SKIN", Map.of("url", "https://crafatar.com/skins/" + uuid))));
            String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            return new Property("textures", base64, null);
        } catch (Exception e) {
            plugin.getComponentLogger().warn("Failed to generate skin from Crafatar for '{}': {}", playerName, e.getMessage());
            return null;
        }
    }

    private JsonObject getJsonFromUrl(String urlStr) throws Exception {
        URI uri = new URI(urlStr);
        URL url = URL.of(uri, null);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        try (Reader reader = new InputStreamReader(con.getInputStream())) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private void applySkin(GameProfile profile, Property skin) {
        if (Bukkit.isPrimaryThread()) {
            profile.getProperties().put("textures", skin);
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> profile.getProperties().put("textures", skin));
        }
    }
}
