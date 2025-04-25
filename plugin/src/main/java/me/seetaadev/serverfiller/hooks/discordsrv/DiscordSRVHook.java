package me.seetaadev.serverfiller.hooks.discordsrv;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.SchedulerUtil;
import me.seetaadev.serverfiller.bot.Bot;

public class DiscordSRVHook {

    public void sendDiscordMessage(String message, Bot bot) {
        SchedulerUtil.runTaskAsynchronously(DiscordSRV.getPlugin(), () -> {
            DiscordSRV.getPlugin().processChatMessage(
                    bot,
                    message,
                    DiscordSRV.getPlugin().getOptionalChannel("global"),
                    false, null);
        });
    }
}
