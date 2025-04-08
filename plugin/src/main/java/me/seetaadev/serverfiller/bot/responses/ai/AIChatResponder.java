package me.seetaadev.serverfiller.bot.responses.ai;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.config.ConfigFile;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AIChatResponder {

    private final ServerFillerPlugin plugin;
    private final OkHttpClient client = new OkHttpClient();
    private final String API_URL = "https://api.together.xyz/v1/chat/completions";

    private boolean enabled;
    private String apiKey;
    private int minReplies;
    private int maxReplies;
    private int minDelay;
    private int maxDelay;

    public AIChatResponder(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        ConfigFile config = new ConfigFile(plugin, null, "config", true);
        this.enabled = config.getConfig().getBoolean("openai.enabled");
        this.apiKey = config.getConfig().getString("openai.apiKey");
        this.minReplies = config.getConfig().getInt("openai.minReplies");
        this.maxReplies = config.getConfig().getInt("openai.maxReplies");
        this.minDelay = config.getConfig().getInt("openai.minDelay");
        this.maxDelay = config.getConfig().getInt("openai.maxDelay");
    }

    public void reload() {
        load();
    }

    public List<String> getResponses(String userMessage, String systemPrompt, int numResponses, String apiKey) throws IOException {
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
        messages.put(new JSONObject().put("role", "user").put("content", userMessage));

        JSONObject payload = new JSONObject()
                .put("model", "meta-llama/Llama-3.3-70B-Instruct-Turbo-Free")
                .put("messages", messages)
                .put("n", numResponses)
                .put("max_tokens", 50)
                .put("temperature", 0.7);

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(payload.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            if (json.has("error")) {
                throw new IOException("Error de Together AI: " + json.getJSONObject("error").getString("message"));
            }

            List<String> replies = new ArrayList<>();
            JSONArray choices = json.getJSONArray("choices");
            for (int i = 0; i < choices.length(); i++) {
                String message = choices.getJSONObject(i).getJSONObject("message").getString("content");
                plugin.getComponentLogger().warn("Message {}: {}", i, message);
                replies.add(message);
            }
            return replies;
        }
    }


    public boolean isAIEnabled() {
        return enabled;
    }

    public String getAPIKey() {
        return apiKey;
    }

    public int getMinReplies() {
        return minReplies;
    }

    public int getMaxReplies() {
        return maxReplies;
    }

    public int getMinDelay() {
        return minDelay;
    }

    public int getMaxDelay() {
        return maxDelay;
    }
}
