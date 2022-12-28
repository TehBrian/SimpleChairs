package dev.tehbrian.simplechairs.api;

import dev.tehbrian.simplechairs.PlayerSitData;
import dev.tehbrian.simplechairs.SimpleChairs;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class ChairsAPI {

  private ChairsAPI() {
  }

  private static PlayerSitData getPlayerSitData() {
    return SimpleChairs.getInstance().getPlayerSitData();
  }

  public static boolean isSitting(final Player player) {
    return getPlayerSitData().isSitting(player);
  }

  public static boolean isBlockOccupied(final Block block) {
    return getPlayerSitData().isBlockOccupied(block);
  }

  public static Player getBlockOccupiedBy(final Block block) {
    return getPlayerSitData().getPlayerOnChair(block);
  }

  public static boolean sit(final Player player, final Block blockToOccupy, final Location sitLocation) {
    return getPlayerSitData().sitPlayer(player, blockToOccupy, sitLocation);
  }

  public static void unsit(final Player player) {
    getPlayerSitData().unsitPlayerForce(player, true);
  }

  public static void setSittingDisabled(final Player player, final boolean bool) {
    getPlayerSitData().setSittingDisabled(player, bool);
  }

  public static boolean isSittingDisabled(final Player player) {
    return getPlayerSitData().isSittingDisabled(player);
  }

}
