package dev.tehbrian.simplechairs.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface SimpleChairs {

  boolean isSitting(Player player);

  boolean isBlockOccupied(Block block);

  Player getBlockOccupiedBy(Block block);

  boolean sit(Player player, Block blockToOccupy, Location sitLocation);

  void unsit(Player player);

  void setSittingDisabled(Player player, boolean bool);

  boolean isSittingDisabled(Player player);

}
