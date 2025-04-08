package me.seetaadev.serverfiller.bot.settings;

import java.util.Objects;
import java.util.UUID;

public class BotSettings {

    private final String name;
    private final String rank;
    private final int skillLevel;
    private final UUID uuid;

    public BotSettings (String name, String rank, int skillLevel, UUID uuid) {
        this.name = name;
        this.rank = rank;
        this.skillLevel = skillLevel;
        this.uuid = Objects.requireNonNullElseGet(uuid, UUID::randomUUID);
    }

    public BotSettings (String name, String rank, int skillLevel) {
        this(name, rank, skillLevel, UUID.randomUUID());
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

}
