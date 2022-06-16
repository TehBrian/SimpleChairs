package dev.tehbrian.simplechairs.command;

import dev.tehbrian.simplechairs.SimpleChairs;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.tehbrian.simplechairs.config.ChairsConfig;
import dev.tehbrian.simplechairs.PlayerSitData;

public class ChairsCommand implements CommandExecutor {

	protected final SimpleChairs plugin;
	protected final ChairsConfig config;
	protected final PlayerSitData sitdata;

	public ChairsCommand(SimpleChairs plugin) {
		this.plugin = plugin;
		this.config = plugin.getChairsConfig();
		this.sitdata = plugin.getPlayerSitData();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}
		if (args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("chairs.reload")) {
				plugin.reloadConfig();
				sender.sendMessage(ChatColor.GREEN + "Chairs configuration reloaded.");
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			}
		}
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args[0].equalsIgnoreCase("off")) {
				sitdata.disableSitting(player);
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.msgSitDisabled));
			} else if (args[0].equalsIgnoreCase("on")) {
				sitdata.enableSitting(player);
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.msgSitEnabled));
			}
		}
		return true;
	}

}
