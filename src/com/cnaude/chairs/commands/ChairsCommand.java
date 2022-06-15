package com.cnaude.chairs.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cnaude.chairs.core.Chairs;
import com.cnaude.chairs.core.ChairsConfig;
import com.cnaude.chairs.core.PlayerSitData;

public class ChairsCommand implements CommandExecutor {

	protected final Chairs plugin;
	protected final ChairsConfig config;
	protected final PlayerSitData sitdata;

	public ChairsCommand(Chairs plugin) {
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
