package me.seetaadev.serverfiller.hooks.voting;

import com.bencodez.votingplugin.VotingPluginHooks;
import com.bencodez.votingplugin.events.PlayerVoteEvent;
import com.bencodez.votingplugin.objects.VoteSite;
import com.bencodez.votingplugin.user.VotingPluginUser;
import me.seetaadev.serverfiller.ServerFillerPlugin;
import me.seetaadev.serverfiller.bot.Bot;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Random;

public class VotingHook {

    private final Random rand = new Random();
    private final ServerFillerPlugin plugin;

    public VotingHook(ServerFillerPlugin plugin) {
        this.plugin = plugin;
    }

    public void vote(Bot bot) {
        VotingPluginHooks votingPlugin = VotingPluginHooks.getInstance();
        List<VoteSite> voteSites = votingPlugin.getMainClass().getVoteSites();

        if (voteSites.isEmpty()) return;

        VoteSite voteSite = voteSites.get(rand.nextInt(voteSites.size()));
        if (voteSite == null) return;

        VotingPluginUser user = votingPlugin.getUserManager().getVotingPluginUser(bot.getUniqueId());
        if (!user.canVoteSite(voteSite)) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerVoteEvent event = new PlayerVoteEvent(
                    voteSite,
                    bot.getName(),
                    voteSite.getServiceSite(),
                    true
            );
            Bukkit.getPluginManager().callEvent(event);
        });
    }
}
