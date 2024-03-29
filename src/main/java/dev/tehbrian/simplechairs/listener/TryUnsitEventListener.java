package dev.tehbrian.simplechairs.listener;

import dev.tehbrian.simplechairs.PlayerSitData;
import dev.tehbrian.simplechairs.SimpleChairsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TryUnsitEventListener implements Listener {

  private final SimpleChairsPlugin plugin;
  private final Map<UUID, Location> dismountTeleport = new HashMap<>();

  public TryUnsitEventListener(final SimpleChairsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerTeleport(final PlayerTeleportEvent event) {
    final Player player = event.getPlayer();
    final PlayerSitData sitData = this.plugin.getPlayerSitData();
    if (sitData.isSitting(player)) {
      sitData.unsitPlayerForce(player, false);
    } else if (event.getCause() == TeleportCause.UNKNOWN) {
      final Location preDismountLocation = this.dismountTeleport.remove(player.getUniqueId());
      if (preDismountLocation != null) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerQuit(final PlayerQuitEvent event) {
    final Player player = event.getPlayer();
    final PlayerSitData sitData = this.plugin.getPlayerSitData();
    if (sitData.isSitting(player)) {
      sitData.unsitPlayerForce(player, true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerDeath(final PlayerDeathEvent event) {
    final Player player = event.getEntity();
    final PlayerSitData sitData = this.plugin.getPlayerSitData();
    if (sitData.isSitting(player)) {
      sitData.unsitPlayerForce(player, false);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockBreak(final BlockBreakEvent event) {
    final Block b = event.getBlock();
    final PlayerSitData sitData = this.plugin.getPlayerSitData();
    if (sitData.isBlockOccupied(b)) {
      final Player player = sitData.getPlayerOnChair(b);
      sitData.unsitPlayerForce(player, true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onExitVehicle(final EntityDismountEvent e) {
    if (e.getEntity() instanceof final Player player) {
      final PlayerSitData sitData = this.plugin.getPlayerSitData();
      if (sitData.isSitting(player)) {
        final Location preDismountLocation = player.getLocation();
        if (!sitData.unsitPlayer(player)) {
          e.setCancelled(true);
        } else {
          final UUID playerUuid = player.getUniqueId();
          this.dismountTeleport.put(playerUuid, preDismountLocation);
          Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.dismountTeleport.remove(playerUuid));
        }
      }
    }
  }

}
