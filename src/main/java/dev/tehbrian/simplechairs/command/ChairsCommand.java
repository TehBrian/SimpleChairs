package dev.tehbrian.simplechairs.command;

import dev.tehbrian.simplechairs.LegacyFormatting;
import dev.tehbrian.simplechairs.PlayerSitData;
import dev.tehbrian.simplechairs.SimpleChairs;
import dev.tehbrian.simplechairs.config.ChairsConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Locale;

public final class ChairsCommand implements CommandExecutor {

    private final SimpleChairs plugin;
    private final ChairsConfig config;
    private final PlayerSitData sitData;

    public ChairsCommand(final SimpleChairs plugin) {
        this.plugin = plugin;
        this.config = plugin.getChairsConfig();
        this.sitData = plugin.getPlayerSitData();
    }

    @Override
    public boolean onCommand(
            final @NonNull CommandSender sender,
            final @NonNull Command command,
            final @NonNull String label,
            final String[] args
    ) {
        if (args.length == 0) {
            return false;
        }

        final var lowercaseArg = args[0].toLowerCase(Locale.ROOT);

        if (lowercaseArg.equals("reload")) {
            if (sender.hasPermission("chairs.reload")) {
                this.plugin.reloadConfig();
                sender.sendMessage(LegacyFormatting.on("&cChairs configuration reloaded."));
            } else {
                sender.sendMessage(LegacyFormatting.on("&cYou do not have permission to do this!"));
            }
        }
        if (sender instanceof final Player player) {
            if (lowercaseArg.equals("off")) {
                this.sitData.setSittingDisabled(player, true);
                player.sendMessage(LegacyFormatting.on(this.config.msgSitDisabled()));
            } else if (lowercaseArg.equals("on")) {
                this.sitData.setSittingDisabled(player, false);
                player.sendMessage(LegacyFormatting.on(this.config.msgSitEnabled()));
            }
        }

        return true;
    }

}
