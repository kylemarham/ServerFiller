package me.seetaadev.serverfiller.commands;

import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.Bot;
import me.seetaadev.serverfiller.bot.BotFactory;
import me.seetaadev.serverfiller.bot.settings.BotSettings;
import me.seetaadev.serverfiller.messages.MessageHandler;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final ServerFillerPlugin plugin;
    private final MessageHandler messageHandler;
    private final BotFactory botFactory;

    public MainCommand(ServerFillerPlugin plugin) {
        this.plugin = plugin;
        this.messageHandler = plugin.getMessageHandler();
        this.botFactory = plugin.getBotFactory();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("serverfiller.main")) {
            sender.sendMessage(messageHandler.format("no_permission", Map.of()));
            return true;
        }

        if (args.length == 0) {
            messageHandler.formatList("help", Map.of()).forEach(sender::sendMessage);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "help":
                messageHandler.formatList("help", Map.of()).forEach(sender::sendMessage);
                break;
            case "reload":
                plugin.reload();
                sender.sendMessage(messageHandler.format("reload", Map.of()));
                break;
            case "spawn":
                String botName = args[1];
                Bot bot = botFactory.getBot(botName);
                if (bot != null) {
                    bot.spawn();
                    sender.sendMessage(messageHandler.format("spawn", Map.of("bot_name", botName)));
                } else {
                    sender.sendMessage(messageHandler.format("botNotFound", Map.of("bot_name", botName)));
                }
                break;
            case "despawn":
                String botNameToDespawn = args[1];
                Bot botToDespawn = botFactory.getBot(botNameToDespawn);
                if (botToDespawn != null) {
                    botToDespawn.despawn();
                    sender.sendMessage(messageHandler.format("despawn", Map.of("bot_name", botNameToDespawn)));
                } else {
                    sender.sendMessage(messageHandler.format("botNotFound", Map.of("bot_name", botNameToDespawn)));
                }
                break;
            case "tpall":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be executed by a player!");
                    break;
                }
                Player player = (Player) sender;
                Location target = player.getLocation();
                List<Bot> onlineBots = botFactory.getOnlineBots();

                for (Bot abot : onlineBots) {
                    // Retrieve the Bukkit player instance for the bot.
                    Player botPlayer = Bukkit.getPlayer(abot.getName());
                    if (botPlayer != null) {
                        botPlayer.teleport(target);
                    }
                }

                player.sendMessage("Teleported " + onlineBots.size() + " bots to your location.");
                break;
            case "create":
                String botId = args[1];
                String rank = args[2];
                int skillLevel = Integer.parseInt(args[3]);

                BotSettings settings = new BotSettings(botId, rank, skillLevel);
                Bot newBot = botFactory.loadBot(settings);
                sender.sendMessage(messageHandler.format("create", Map.of("bot_name", newBot.getName())));
                break;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("serverfiller.main")) {
            return List.of();
        }

        if (args.length == 1) {
            return List.of("help", "reload", "spawn", "despawn", "create");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("despawn")) {
                return botFactory.getBots().values().stream().map(CraftHumanEntity::getName).collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("create")) {
                return List.of("<name>");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return LuckPermsProvider.get().getGroupManager().getLoadedGroups().stream()
                    .map(Group::getName)
                    .collect(Collectors.toList());
        } else if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            return List.of("<skillLevel>");
        }

        return List.of();
    }
}
