package com.cnaude.chairs.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

import com.cnaude.chairs.core.Chairs;

public class TryUnsitEventListener implements Listener {

	protected final Chairs plugin;
	public TryUnsitEventListener(Chairs plugin) {
		this.plugin = plugin;
	}

	protected Map<UUID, Location> dismountTeleport = new HashMap<>();

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
		if (plugin.getPlayerSitData().isSitting(player)) {
			plugin.getPlayerSitData().unsitPlayerForce(player, false);
		} else if (event.getCause() == TeleportCause.UNKNOWN) {
			Location preDismountLocation = dismountTeleport.remove(player.getUniqueId());
			if (preDismountLocation != null) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (plugin.getPlayerSitData().isSitting(player)) {
			plugin.getPlayerSitData().unsitPlayerForce(player, true);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (plugin.getPlayerSitData().isSitting(player)) {
			plugin.getPlayerSitData().unsitPlayerForce(player, false);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		Block b = event.getBlock();
		if (plugin.getPlayerSitData().isBlockOccupied(b)) {
			Player player = plugin.getPlayerSitData().getPlayerOnChair(b);
			plugin.getPlayerSitData().unsitPlayerForce(player, true);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onExitVehicle(EntityDismountEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			if (plugin.getPlayerSitData().isSitting(player)) {
				Location preDismountLocation = player.getLocation();
				if (!plugin.getPlayerSitData().unsitPlayer(player)) {
					e.setCancelled(true);
				} else {
					UUID playerUUID = player.getUniqueId();
					dismountTeleport.put(playerUUID, preDismountLocation);
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> dismountTeleport.remove(playerUUID));
				}
			}
		}
	}

}
