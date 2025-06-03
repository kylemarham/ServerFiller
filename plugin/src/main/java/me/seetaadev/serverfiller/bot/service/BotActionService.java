package me.seetaadev.serverfiller.bot.service;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.service.actions.Action;
import me.seetaadev.serverfiller.bot.service.actions.BuyAction;
import me.seetaadev.serverfiller.bot.service.actions.LoginAction;
import me.seetaadev.serverfiller.bot.service.actions.VoteAction;
import me.seetaadev.serverfiller.bot.settings.BotActionSettings;
import me.seetaadev.serverfiller.bot.settings.ItemSettings;

import java.util.Set;

public class BotActionService {

    private final ServerFillerPlugin plugin;
    private BotActionSettings config;
    private ItemSettings itemSettings;
    private final Set<Action> actions;

    public BotActionService(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        this.config = new BotActionSettings(plugin);
        this.itemSettings = new ItemSettings(plugin);
        this.actions = Set.of(
                new LoginAction(plugin, this),
                new VoteAction(plugin, this),
                new BuyAction(plugin, this)
        );
    }

    public void load() {
        this.config = new BotActionSettings(plugin);
        this.itemSettings = new ItemSettings(plugin);
    }

    public void reload() {
        load();
        start();
    }

    public void start() {
        stop();
        actions.forEach(Action::start);
    }

    public void stop() {
        actions.forEach(Action::stop);
    }

    public void ensureMinimum() {
        actions.forEach(Action::ensureMinimum);
    }

    public BotActionSettings getConfig() {
        return config;
    }

    public ItemSettings getItemSettings() {
        return itemSettings;
    }
}
