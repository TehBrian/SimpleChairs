package dev.tehbrian.simplechairs.listener;

import dev.tehbrian.simplechairs.PlayerSitService;
import dev.tehbrian.simplechairs.SimpleChairsPlugin;
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
    final PlayerSitService sitService = this.plugin.getSitService();
    if (sitService.isSitting(player)) {
      sitService.unsitForce(player, false);
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
    final PlayerSitService sitService = this.plugin.getSitService();
    if (sitService.isSitting(player)) {
      sitService.unsitForce(player, true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerDeath(final PlayerDeathEvent event) {
    final Player player = event.getEntity();
    final PlayerSitService sitService = this.plugin.getSitService();
    if (sitService.isSitting(player)) {
      sitService.unsitForce(player, false);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockBreak(final BlockBreakEvent event) {
    final Block block = event.getBlock();
    final PlayerSitService sitService = this.plugin.getSitService();
    if (sitService.isBlockOccupied(block)) {
      final Player player = sitService.getBlockOccupant(block);
      sitService.unsitForce(player, true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onExitVehicle(final EntityDismountEvent e) {
    if (e.getEntity() instanceof final Player player) {
      final PlayerSitService sitService = this.plugin.getSitService();
      if (sitService.isSitting(player)) {
        final Location preDismountLocation = player.getLocation();
        if (!sitService.unsit(player)) {
          e.setCancelled(true);
        } else {
          final UUID playerUuid = player.getUniqueId();
          this.dismountTeleport.put(playerUuid, preDismountLocation);
          player.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.dismountTeleport.remove(playerUuid));
        }
      }
    }
  }

}
