package dev.tehbrian.simplechairs.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public interface SimpleChairs {

	boolean isSitting(Player player);

	boolean sit(Player player, Block blockToOccupy, Location perch);

	void unsit(Player player);

	boolean isBlockOccupied(Block block);

	@Nullable Player getBlockOccupant(Block block);

	boolean isSittingDisabled(Player player);

	void setSittingDisabled(Player player, boolean sittingDisabled);

}
