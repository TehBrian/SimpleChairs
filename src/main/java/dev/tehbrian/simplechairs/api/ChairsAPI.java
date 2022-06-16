package dev.tehbrian.simplechairs.api;

import dev.tehbrian.simplechairs.Chairs;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import dev.tehbrian.simplechairs.PlayerSitData;

public class ChairsAPI {

	private static PlayerSitData getPlayerSitData() {
		return Chairs.getInstance().getPlayerSitData();
	}

	public static boolean isSitting(Player player) {
		return getPlayerSitData().isSitting(player);
	}

	public static boolean isBlockOccupied(Block block) {
		return getPlayerSitData().isBlockOccupied(block);
	}

	public static Player getBlockOccupiedBy(Block block) {
		return getPlayerSitData().getPlayerOnChair(block);
	}

	public static boolean sit(Player player, Block blocktouccupy, Location sitlocation) {
		return getPlayerSitData().sitPlayer(player, blocktouccupy, sitlocation);
	}

	public static void unsit(Player player) {
		getPlayerSitData().unsitPlayerForce(player, true);
	}

	public static void disableSitting(Player player) {
		getPlayerSitData().disableSitting(player);
	}

	public static void enableSitting(Player player) {
		getPlayerSitData().enableSitting(player);
	}

	public static boolean isSittingDisabled(Player player) {
		return getPlayerSitData().isSittingDisabled(player);
	}

}
