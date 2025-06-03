package me.seetaadev.serverfiller.bot.service.actions;

import java.util.Random;

public interface Action {

    Random rand = new Random();
    void start();
    void stop();

    default int getRandomDelayTicks(int min, int max) {
        int range = Math.max(max - min + 1, 1);
        return (min + rand.nextInt(range)) * 20;
    }

    default void ensureMinimum() {}
}
