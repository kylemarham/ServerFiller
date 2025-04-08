package me.seetaadev.serverfiller.bot.personality;

public class Personality {

    private final String name;
    private final String systemPrompt;
    private final double chance;
    private final String tone;
    private final String vocabulary;

    public Personality(String name, double chance, String tone, String vocabulary, String prompt) {
        this.name = name;
        this.chance = chance;
        this.tone = tone;
        this.vocabulary = vocabulary;
        this.systemPrompt = prompt;
    }

    public String getName() {
        return name;
    }

    public String getSystemPrompt() {
        return systemPrompt + " with a vocabulary of " + vocabulary + " and a tone of " + tone + ".";
    }

    public double getChance() {
        return chance;
    }
}
