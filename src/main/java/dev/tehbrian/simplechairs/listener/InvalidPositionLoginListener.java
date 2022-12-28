package dev.tehbrian.simplechairs.listener;

import dev.tehbrian.simplechairs.SimpleChairs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.slf4j.Logger;

public final class InvalidPositionLoginListener implements Listener {

  private final Logger logger;

  public InvalidPositionLoginListener(final SimpleChairs plugin) {
    this.logger = plugin.getSLF4JLogger();
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPlayerJoin(final PlayerJoinEvent event) {
    final Player player = event.getPlayer();

    if (Double.isNaN(player.getLocation().getY())) {
      this.logger.warn(
          "Player `{}` joined with an invalid position. Teleporting them to world spawn.",
          player.getName()
      );
      player.teleport(player.getWorld().getSpawnLocation());
    }
  }

}
