package me.seetaadev.serverfiller.bot.settings;

import java.util.Objects;
import java.util.UUID;

public class BotSettings {

    private final String name;
    private final String rank;
    private final int skillLevel;
    private final UUID uuid;
    private final boolean hasPlayedBefore;

    public BotSettings (String name, String rank, int skillLevel, UUID uuid, boolean hasPlayedBefore) {
        this.name = name;
        this.rank = rank;
        this.skillLevel = skillLevel;
        this.uuid = Objects.requireNonNullElseGet(uuid, UUID::randomUUID);
        this.hasPlayedBefore = hasPlayedBefore;
    }

    public BotSettings (String name, String rank, int skillLevel) {
        this(name, rank, skillLevel, UUID.randomUUID(), false);
    }

    public String getName() {
        return name;
    }

    public String getRank() {
        return rank;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean hasPlayedBefore() {
        return hasPlayedBefore;
    }

}
