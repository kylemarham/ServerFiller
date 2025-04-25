package me.seetaadev.serverfiller;

import me.seetaadev.serverfiller.bot.BotFactory;
import me.seetaadev.serverfiller.bot.personality.PersonalityManager;
import me.seetaadev.serverfiller.bot.responses.local.ChatResponder;
import me.seetaadev.serverfiller.bot.service.BotActionService;
import me.seetaadev.serverfiller.bot.service.BotMessageService;
import me.seetaadev.serverfiller.commands.MainCommand;
import me.seetaadev.serverfiller.hooks.HookManager;
import me.seetaadev.serverfiller.listeners.BotJoinListener;
import me.seetaadev.serverfiller.listeners.BotLeaveListener;
import me.seetaadev.serverfiller.listeners.ChatListener;
import me.seetaadev.serverfiller.messages.MessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerFillerPlugin extends JavaPlugin {

    private final BotFactory botFactory = new BotFactory(this);
    private final MessageHandler messageHandler = new MessageHandler(this);
    private final PersonalityManager personalityManager = new PersonalityManager(this);
    private final ChatResponder chatResponder = new ChatResponder(this);
    private final BotActionService botActionService = new BotActionService(this);
    private final BotMessageService botMessageService = new BotMessageService(this);
    private final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService botExecutor = Executors.newFixedThreadPool(10);
    private final HookManager hookManager = new HookManager(this);

    @Override
    public void onEnable() {
        botFactory.load();
        messageHandler.load();
        botMessageService.load();
        personalityManager.load();
        chatResponder.load();
        botActionService.load();

        Bukkit.getPluginManager().registerEvents(new BotJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BotLeaveListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Objects.requireNonNull(getCommand("serverfiller")).setExecutor(new MainCommand(this));

        new BukkitRunnable() {
            @Override
            public void run() {
                hookManager.init();
                botActionService.start();
                botActionService.ensureMinimum();
            }
        }.runTaskLaterAsynchronously(this, 20L * 3);
    }

    public void reload() {
        botFactory.reload();
        messageHandler.reload();
        botMessageService.reload();
        personalityManager.reload();
        chatResponder.reload();
        botActionService.reload();
    }

    @Override
    public void onDisable() {
        if (!asyncExecutor.isShutdown()) {
            asyncExecutor.shutdownNow();
        }

        if (!botExecutor.isShutdown()) {
            botExecutor.shutdownNow();
        }

        botActionService.stop();
    }

    public BotFactory getBotFactory() {
        return botFactory;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public PersonalityManager getPersonalityManager() {
        return personalityManager;
    }

    public ChatResponder getChatResponder() {
        return chatResponder;
    }

    public BotMessageService getBotMessageService() {
        return botMessageService;
    }

    public ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public ExecutorService getBotExecutor() {
        return botExecutor;
    }

    public HookManager getHookManager() {
        return hookManager;
    }
}
