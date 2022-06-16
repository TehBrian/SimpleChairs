package dev.tehbrian.simplechairs.command;

import dev.tehbrian.simplechairs.PlayerSitData;
import dev.tehbrian.simplechairs.SimpleChairs;
import dev.tehbrian.simplechairs.config.ChairsConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChairsCommand implements CommandExecutor {

    protected final SimpleChairs plugin;
    protected final ChairsConfig config;
    protected final PlayerSitData sitData;

    public ChairsCommand(final SimpleChairs plugin) {
        this.plugin = plugin;
        this.config = plugin.getChairsConfig();
        this.sitData = plugin.getPlayerSitData();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            return false;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("chairs.reload")) {
                this.plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "Chairs configuration reloaded.");
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            }
        }
        if (sender instanceof final Player player) {
            if (args[0].equalsIgnoreCase("off")) {
                this.sitData.disableSitting(player);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.msgSitDisabled));
            } else if (args[0].equalsIgnoreCase("on")) {
                this.sitData.enableSitting(player);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.msgSitEnabled));
            }
        }
        return true;
    }

}
