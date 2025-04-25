package me.seetaadev.serverfiller.bot.cache;

import com.mojang.authlib.properties.Property;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SkinCache {

    private final Map<String, Property> skinByName = new ConcurrentHashMap<>();
    private final Map<String, Property> skinByUUID = new ConcurrentHashMap<>();

    public Property getByName(String name) {
        return skinByName.get(name.toLowerCase());
    }

    public void putByName(String name, Property property) {
        skinByName.put(name.toLowerCase(), property);
    }

    public Property getByUUID(String uuid) {
        return skinByUUID.get(uuid);
    }

    public void putByUUID(String uuid, Property property) {
        skinByUUID.put(uuid, property);
    }
}
