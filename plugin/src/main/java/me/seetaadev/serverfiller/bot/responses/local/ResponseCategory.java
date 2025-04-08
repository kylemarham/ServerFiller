package me.seetaadev.serverfiller.bot.responses.local;

import java.util.List;

public record ResponseCategory(List<String> keywords, List<String> responses) {

    public boolean matches(String message) {
        for (String keyword : keywords) {
            if (message.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public String getRandomResponse() {
        int randomIndex = (int) (Math.random() * responses.size());
        return responses.get(randomIndex);
    }
}
