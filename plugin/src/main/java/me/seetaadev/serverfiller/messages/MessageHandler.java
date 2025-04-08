package me.seetaadev.serverfiller.messages;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.config.ConfigFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageHandler {

    private final ServerFillerPlugin plugin;
    private final Map<String, Object> messages;
    private final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public MessageHandler(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
    }

    public void load() {
        ConfigFile configFile = new ConfigFile(plugin, null, "config", true);
        FileConfiguration config = configFile.getConfig();
        ConfigurationSection section = config.getConfigurationSection("messages");

        if (section != null) {
            for (String key : section.getKeys(true)) {
                Object value = section.get(key);
                if (value instanceof String || value instanceof List) {
                    messages.put(key, value);
                }
            }
        }
    }

    public String getMessage(String key) {
        Object value = messages.get(key);
        return value instanceof String ? (String) value : "Message not found: " + key;
    }

    public List<String> getMessageList(String key) {
        Object value = messages.get(key);
        return value instanceof List ? (List<String>) value : List.of("Message list not found: " + key);
    }

    public void reload() {
        messages.clear();
        load();
    }

    public Component format(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        if (message == null) {
            return Component.text("Message not found: " + path);
        }
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return format(message);
    }

    public List<Component> formatList(String path, Map<String, String> placeholders) {
        List<String> messages = getMessageList(path);
        if (messages == null) {
            return List.of(Component.text("Message list not found: " + path));
        }
        for (int i = 0; i < messages.size(); i++) {
            String message = messages.get(i);
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            messages.set(i, message);
        }

        return messages.stream()
                .map(this::format)
                .toList();
    }

    public Component format(String message) {
        String processed = message
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>");

        processed = processed
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");

        processed = processed.replace("{", "<").replace("}", ">");
        return MINI_MESSAGE.deserialize(processed);
    }
}
