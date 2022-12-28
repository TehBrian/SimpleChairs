package dev.tehbrian.simplechairs.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

/**
 * Teleports players to the world spawn if they join with an invalid position.
 * <p>
 * The origin of this class is commit d5bfae0 by Shevchik.
 * <a href="https://github.com/Shevchik/Chairs/commit/d5bfae0d2fdd13e89d097186bc54e85e6ab0c2ceI">Here it is on GitHub.</a>.
 * <p>
 * I'm not sure if this listener is still needed, but let's keep it just in case.
 */
public final class InvalidPositionLoginListener implements Listener {

  private final Logger logger;

  public InvalidPositionLoginListener(final JavaPlugin plugin) {
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
