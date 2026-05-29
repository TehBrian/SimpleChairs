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
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
			return;
		}

		final Player player = event.getPlayer();
		final Block block = event.getClickedBlock();

		if (this.plugin.getSitUtils().calculatePerch(player, block) == null) {
			return;
		}

		// now that we know that the chair is valid, cancel the event.
		event.setCancelled(true);

		// defer the teleport/mount until after Paper finishes this interact packet.
		// fixes weird `Height limit for building is 319` message.
		this.plugin.getServer().getScheduler().runTask(
				this.plugin,
				() -> {
					if (!player.isOnline()) {
						return;
					}

					// recalculate in case the player, chair, or occupancy changed during that tick.
					final Location perch = this.plugin.getSitUtils().calculatePerch(player, block);
					if (perch == null) {
						return;
					}

					this.plugin.getSitService().sit(player, block, perch);
				}
		);
	}

}
