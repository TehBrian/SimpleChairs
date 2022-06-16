package dev.tehbrian.simplechairs.listener;

import dev.tehbrian.simplechairs.SimpleChairs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TryUnsitEventListener implements Listener {

    protected final SimpleChairs plugin;

    public TryUnsitEventListener(final SimpleChairs plugin) {
        this.plugin = plugin;
    }

    protected Map<UUID, Location> dismountTeleport = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        if (this.plugin.getPlayerSitData().isSitting(player)) {
            this.plugin.getPlayerSitData().unsitPlayerForce(player, false);
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
        if (this.plugin.getPlayerSitData().isSitting(player)) {
            this.plugin.getPlayerSitData().unsitPlayerForce(player, true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        if (this.plugin.getPlayerSitData().isSitting(player)) {
            this.plugin.getPlayerSitData().unsitPlayerForce(player, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block b = event.getBlock();
        if (this.plugin.getPlayerSitData().isBlockOccupied(b)) {
            final Player player = this.plugin.getPlayerSitData().getPlayerOnChair(b);
            this.plugin.getPlayerSitData().unsitPlayerForce(player, true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExitVehicle(final EntityDismountEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player player = (Player) e.getEntity();
            if (this.plugin.getPlayerSitData().isSitting(player)) {
                final Location preDismountLocation = player.getLocation();
                if (!this.plugin.getPlayerSitData().unsitPlayer(player)) {
                    e.setCancelled(true);
                } else {
                    final UUID playerUUID = player.getUniqueId();
                    this.dismountTeleport.put(playerUUID, preDismountLocation);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.dismountTeleport.remove(playerUUID));
                }
            }
        }
    }

}
