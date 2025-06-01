package me.seetaadev.serverfiller;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class RedisMessage {

    private final Payload payload;
    private final Map<String, String> params;

    public Payload getPayload() {
        return payload;
    }

    public RedisMessage(Payload payload) {
        this.payload = payload;
        this.params = new HashMap<>();
    }

    public RedisMessage setParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    public String getParam(String key) {
        return params.getOrDefault(key, null);
    }

    public boolean containsParam(String key) {
        return params.containsKey(key);
    }

    public void removeParam(String key) {
        params.remove(key);
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }
}
