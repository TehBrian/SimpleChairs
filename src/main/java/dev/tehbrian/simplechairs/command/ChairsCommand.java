package dev.tehbrian.simplechairs.command;

import dev.tehbrian.simplechairs.LegacyFormatting;
import dev.tehbrian.simplechairs.PlayerSitData;
import dev.tehbrian.simplechairs.SimpleChairsPlugin;
import dev.tehbrian.simplechairs.config.ChairsConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class ChairsCommand implements CommandExecutor {

  private final SimpleChairsPlugin plugin;
  private final ChairsConfig config;
  private final PlayerSitData sitData;

  public ChairsCommand(final SimpleChairsPlugin plugin) {
    this.plugin = plugin;
    this.config = plugin.getChairsConfig();
    this.sitData = plugin.getPlayerSitData();
  }

  @Override
  public boolean onCommand(
      final @NotNull CommandSender sender,
      final @NotNull Command command,
      final @NotNull String label,
      final String[] args
  ) {
    if (args.length == 0) {
      return false;
    }

    final var lowercaseArg = args[0].toLowerCase(Locale.ROOT);

    if (lowercaseArg.equals("reload")) {
      if (sender.hasPermission("chairs.reload")) {
        this.plugin.reloadConfig();
        sender.sendMessage(LegacyFormatting.on("&aChairs configuration reloaded."));
      } else {
        sender.sendMessage(LegacyFormatting.on("&cYou don't have permission to do this."));
      }
      return true;
    }

    if (sender instanceof final Player player) {
      switch (lowercaseArg) {
        case "on" -> {
          this.sitData.setSittingDisabled(player, false);
          player.sendMessage(LegacyFormatting.on(this.config.msgSitEnabled()));
        }
        case "off" -> {
          this.sitData.setSittingDisabled(player, true);
          player.sendMessage(LegacyFormatting.on(this.config.msgSitDisabled()));
        }
        default -> {
          return false;
        }
      }
    }

    return true;
  }

}
