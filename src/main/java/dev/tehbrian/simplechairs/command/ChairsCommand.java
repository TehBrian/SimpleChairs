package dev.tehbrian.simplechairs.command;

import dev.tehbrian.simplechairs.LegacyFormatting;
import dev.tehbrian.simplechairs.Permissions;
import dev.tehbrian.simplechairs.SimpleChairsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ChairsCommand implements TabExecutor {

  private final SimpleChairsPlugin plugin;

  public ChairsCommand(final SimpleChairsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(
      final @NotNull CommandSender sender,
      final @NotNull Command command,
      final @NotNull String label,
      final @NotNull String[] args
  ) {
    if (args.length == 0) {
      return false;
    }

    final var lowerArg = args[0].toLowerCase(Locale.ROOT);

    if (lowerArg.equals("reload")) {
      if (sender.hasPermission(Permissions.RELOAD)) {
        this.plugin.reloadConfig();
        sender.sendMessage(LegacyFormatting.on("&aChairs configuration reloaded."));
      } else {
        sender.sendMessage(LegacyFormatting.on("&cYou don't have permission to do this."));
      }
      return true;
    }

    if (sender instanceof final Player player) {
      switch (lowerArg) {
        case "on" -> {
          this.plugin.getPlayerSitData().setSittingDisabled(player, false);
          player.sendMessage(LegacyFormatting.on(this.plugin.getChairsConfig().msgSitEnabled()));
        }
        case "off" -> {
          this.plugin.getPlayerSitData().setSittingDisabled(player, true);
          player.sendMessage(LegacyFormatting.on(this.plugin.getChairsConfig().msgSitDisabled()));
        }
        default -> {
          return false;
        }
      }
    }

    return true;
  }

  @Override
  public @NotNull List<String> onTabComplete(
      final @NotNull CommandSender sender,
      final @NotNull Command command,
      final @NotNull String label,
      final @NotNull String[] args
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
