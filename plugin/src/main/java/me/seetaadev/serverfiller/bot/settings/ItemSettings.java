package me.seetaadev.serverfiller.bot.settings;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.config.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ItemSettings {
    private final List<BuyItem> buyItems;

    public ItemSettings (ServerFillerPlugin plugin) {
        FileConfiguration config = new ConfigFile(plugin, null, "items", true).getConfig();
        buyItems = new ArrayList<>();

        for (String key : config.getKeys(false)) {
            String itemName = config.getString(key + ".name");
            double chance = config.getDouble(key + ".chance", 0.0);
            if (itemName != null && chance > 0) {
                buyItems.add(new BuyItem(itemName, chance));
            }
        }
    }

    public BuyItem randomBuyItem() {
        if (buyItems.isEmpty()) return null;

        double totalChance = buyItems.stream().mapToDouble(BuyItem::chance).sum();
        double randomValue = Math.random() * totalChance;

        for (BuyItem item : buyItems) {
            if (randomValue < item.chance()) {
                return item;
            }
            randomValue -= item.chance();
        }

        return null;
    }

    public record BuyItem(String itemName, double chance) {
    }
}
