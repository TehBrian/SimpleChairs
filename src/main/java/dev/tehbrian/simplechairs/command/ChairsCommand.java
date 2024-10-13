package dev.tehbrian.simplechairs.command;

import dev.tehbrian.simplechairs.Permissions;
import dev.tehbrian.simplechairs.SimpleChairsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static dev.tehbrian.simplechairs.Formatting.legacy;

public final class ChairsCommand implements TabExecutor {

	private final SimpleChairsPlugin plugin;

	public ChairsCommand(final SimpleChairsPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(
			final @NonNull CommandSender sender,
			final @NonNull Command command,
			final @NonNull String label,
			final @NonNull String[] args
	) {
		if (args.length == 0) {
			return false;
		}

		final var lowerArg = args[0].toLowerCase(Locale.ROOT);

		if (lowerArg.equals("reload")) {
			if (sender.hasPermission(Permissions.RELOAD)) {
				this.plugin.reloadConfig();
				sender.sendMessage(legacy("&aChairs configuration reloaded."));
			} else {
				sender.sendMessage(legacy("&cYou don't have permission to do this."));
			}
			return true;
		}

		if (sender instanceof final Player player) {
			switch (lowerArg) {
				case "on" -> {
					this.plugin.getSitService().setSittingDisabled(player, false);
					player.sendMessage(legacy(this.plugin.getChairsConfig().msgSitEnabled()));
				}
				case "off" -> {
					this.plugin.getSitService().setSittingDisabled(player, true);
					player.sendMessage(legacy(this.plugin.getChairsConfig().msgSitDisabled()));
				}
				default -> {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public @NonNull List<String> onTabComplete(
			final @NonNull CommandSender sender,
			final @NonNull Command command,
			final @NonNull String label,
			final @NonNull String[] args
	) {
		final List<String> suggestions = new ArrayList<>();

		if (sender.hasPermission(Permissions.RELOAD)) {
			suggestions.add("reload");
		}

		if (sender instanceof Player) {
			suggestions.add("on");
			suggestions.add("off");
		}

		return suggestions;
	}

}
