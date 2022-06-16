package dev.tehbrian.simplechairs.sitaddons;

import dev.tehbrian.simplechairs.PlayerSitData;
import dev.tehbrian.simplechairs.SimpleChairs;
import dev.tehbrian.simplechairs.config.ChairsConfig;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandRestrict implements Listener {

    protected final SimpleChairs plugin;
    protected final ChairsConfig config;
    protected final PlayerSitData sitData;

    public CommandRestrict(final SimpleChairs plugin) {
        this.plugin = plugin;
        this.config = plugin.getChairsConfig();
        this.sitData = plugin.getPlayerSitData();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final String playercommand = event.getMessage().toLowerCase();
        if (this.plugin.getPlayerSitData().isSitting(player)) {
            if (this.config.restrictionsDisableAllCommands) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.msgSitCommandRestricted));
                return;
            }
            for (final String disabledCommand : this.config.restrictionsDisabledCommands) {
                if (playercommand.startsWith(disabledCommand)) {
                    final String therest = playercommand.replace(disabledCommand, "");
                    if (therest.isEmpty() || therest.startsWith(" ")) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.msgSitCommandRestricted));
                        return;
                    }
                }
            }
        }
    }

}
