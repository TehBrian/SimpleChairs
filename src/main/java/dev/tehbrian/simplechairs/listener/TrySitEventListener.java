package dev.tehbrian.simplechairs.listener;

import dev.tehbrian.simplechairs.SimpleChairsPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class TrySitEventListener implements Listener {

	private final SimpleChairsPlugin plugin;

	public TrySitEventListener(final SimpleChairsPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
			final Player player = event.getPlayer();
			final Block block = event.getClickedBlock();

			final Location perch = this.plugin.getSitUtils().calculatePerch(player, block);
			if (perch != null && this.plugin.getSitService().sit(player, block, perch)) {
				event.setCancelled(true);
			}
		}
	}

}
