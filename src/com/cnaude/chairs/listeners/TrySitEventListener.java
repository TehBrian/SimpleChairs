package com.cnaude.chairs.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.cnaude.chairs.core.Chairs;

public class TrySitEventListener implements Listener {

	protected final Chairs plugin;

	public TrySitEventListener(Chairs plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((event.getAction() == Action.RIGHT_CLICK_BLOCK) && (event.getHand() == EquipmentSlot.HAND)) {
			Player player = event.getPlayer();
			Block block = event.getClickedBlock();
			Location sitLocation = plugin.getSitUtils().calculateSitLocation(player, block);
			if ((sitLocation != null) && plugin.getPlayerSitData().sitPlayer(player, block, sitLocation)) {
				event.setCancelled(true);
			}
		}
	}

}