package dev.tehbrian.simplechairs;

import dev.tehbrian.simplechairs.api.PlayerChairSitEvent;
import dev.tehbrian.simplechairs.api.PlayerChairUnsitEvent;
import dev.tehbrian.simplechairs.api.PlayerChairUnsitForceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

import static dev.tehbrian.simplechairs.Formatting.legacy;

public final class PlayerSitService {

	private final SimpleChairsPlugin plugin;

	private final NamespacedKey sitDisabledKey;

	private final Map<Player, SitData> sittingPlayers = new HashMap<>();
	private final Map<Block, Player> occupiedBlocks = new HashMap<>();

	public PlayerSitService(final SimpleChairsPlugin plugin) {
		this.plugin = plugin;
		this.sitDisabledKey = new NamespacedKey(plugin, "SitDisabled");
	}

	public boolean isSitting(final Player player) {
		final SitData sitData = this.sittingPlayers.get(player);
		return (sitData != null) && sitData.sitting;
	}

	public boolean sit(final Player player, final Block blockToOccupy, final Location perch) {
		final var sitEvent = new PlayerChairSitEvent(player, perch.clone());
		Bukkit.getPluginManager().callEvent(sitEvent);
		if (sitEvent.isCancelled()) {
			return false;
		}

		final Location perchAfterEvent = sitEvent.getPerch().clone();
		final Entity chairEntity = perchAfterEvent.getWorld().spawn(perchAfterEvent, TextDisplay.class);

		final SitData sitData = new SitData(player.getLocation(), blockToOccupy, chairEntity, true);


		player.teleport(perchAfterEvent);
		chairEntity.addPassenger(player);

		this.sittingPlayers.put(player, sitData);
    this.occupiedBlocks.put(blockToOccupy, player);

		if (this.plugin.getChairsConfig().msgEnabled()) {
			player.sendMessage(legacy(this.plugin.getChairsConfig().msgSitEnter()));
		}

		return true;
	}

	public boolean unsit(final Player player) {
		return this.unsit(player, true, true);
	}

	public void unsitForce(final Player player, final boolean teleport) {
		this.unsit(player, teleport, false);
	}

	private boolean unsit(final Player player, final boolean teleport, final boolean cancellable) {
		final SitData sitData = this.sittingPlayers.get(player);

		final Location retreat;
		if (cancellable) {
			final var unsitEvent = new PlayerChairUnsitEvent(player, sitData.retreat.clone());
			player.getServer().getPluginManager().callEvent(unsitEvent);

			if (unsitEvent.isCancelled()) {
				return false;
			}

			retreat = unsitEvent.getRetreat().clone();
		} else {
			final var unsitForceEvent = new PlayerChairUnsitForceEvent(player, sitData.retreat.clone());
			player.getServer().getPluginManager().callEvent(unsitForceEvent);

			retreat = unsitForceEvent.getRetreat().clone();
		}

		sitData.sitting = false;
		player.leaveVehicle();
		sitData.mountedEntity.remove();
		player.setSneaking(false);
		this.occupiedBlocks.remove(sitData.occupiedBlock);
		this.sittingPlayers.remove(player);
		if (teleport) {
			player.teleport(retreat);
		}
		if (this.plugin.getChairsConfig().msgEnabled()) {
			player.sendMessage(legacy(this.plugin.getChairsConfig().msgSitLeave()));
		}

		return true;
	}

	public boolean isBlockOccupied(final Block block) {
		return this.occupiedBlocks.containsKey(block);
	}

	public Player getBlockOccupant(final Block chair) {
		return this.occupiedBlocks.get(chair);
	}

	public boolean isSittingDisabled(final Player player) {
		return player.getPersistentDataContainer().getOrDefault(
				this.sitDisabledKey,
				PersistentDataType.BOOLEAN,
				false
		);
	}

	public void setSittingDisabled(final Player player, final boolean bool) {
		if (bool) {
			player.getPersistentDataContainer().set(this.sitDisabledKey, PersistentDataType.BOOLEAN, true);
		} else {
			player.getPersistentDataContainer().remove(this.sitDisabledKey);
		}
	}

	protected static class SitData {

		protected final Location retreat;
		protected final Block occupiedBlock;
		protected final Entity mountedEntity;
		protected boolean sitting;

		public SitData(
				final Location retreat,
				final Block occupiedBlock,
				final Entity mountedEntity,
				final boolean sitting
		) {
			this.retreat = retreat;
			this.occupiedBlock = occupiedBlock;
			this.mountedEntity = mountedEntity;
			this.sitting = sitting;
		}

	}

}
