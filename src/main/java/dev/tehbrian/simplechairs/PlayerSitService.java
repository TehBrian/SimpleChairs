package dev.tehbrian.simplechairs;

import dev.tehbrian.simplechairs.api.PlayerChairSitEvent;
import dev.tehbrian.simplechairs.api.PlayerChairUnsitEvent;
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

  public boolean isSittingDisabled(final Player player) {
    return player.getPersistentDataContainer().getOrDefault(
        this.sitDisabledKey,
        PersistentDataType.BYTE,
        (byte) 0
    ) != 0;
  }

  public void setSittingDisabled(final Player player, final boolean bool) {
    if (bool) {
      player.getPersistentDataContainer().set(this.sitDisabledKey, PersistentDataType.BYTE, (byte) 1);
    } else {
      player.getPersistentDataContainer().remove(this.sitDisabledKey);
    }
  }

  public boolean isSitting(final Player player) {
    final SitData sitData = this.sittingPlayers.get(player);
    return (sitData != null) && sitData.sitting;
  }

  public boolean isBlockOccupied(final Block block) {
    return this.occupiedBlocks.containsKey(block);
  }

  public Player getPlayerOnChair(final Block chair) {
    return this.occupiedBlocks.get(chair);
  }

  public boolean sitPlayer(final Player player, final Block blockToOccupy, final Location sitLoc) {
    final PlayerChairSitEvent playerSitEvent = new PlayerChairSitEvent(player, sitLoc.clone());
    Bukkit.getPluginManager().callEvent(playerSitEvent);
    if (playerSitEvent.isCancelled()) {
      return false;
    }

    final Location postEventSitLoc = playerSitEvent.getSitLocation().clone();
    final Entity chairEntity = postEventSitLoc.getWorld().spawn(postEventSitLoc, TextDisplay.class);

    final SitData sitData = new SitData(player.getLocation(), blockToOccupy, chairEntity, true);

    player.teleport(postEventSitLoc);
    chairEntity.addPassenger(player);

    this.sittingPlayers.put(player, sitData);
    this.occupiedBlocks.put(blockToOccupy, player);

    if (this.plugin.getChairsConfig().msgEnabled()) {
      player.sendMessage(legacy(this.plugin.getChairsConfig().msgSitEnter()));
    }

    return true;
  }

  public boolean unsitPlayer(final Player player) {
    return this.unsitPlayer(player, true, true);
  }

  public void unsitPlayerForce(final Player player, final boolean teleport) {
    this.unsitPlayer(player, false, teleport);
  }

  private boolean unsitPlayer(final Player player, final boolean canCancel, final boolean teleport) {
    final SitData sitData = this.sittingPlayers.get(player);
    final PlayerChairUnsitEvent playerUnsitEvent = new PlayerChairUnsitEvent(
        player,
        sitData.teleportBack.clone(),
        canCancel
    );
    Bukkit.getPluginManager().callEvent(playerUnsitEvent);
    if (playerUnsitEvent.isCancelled() && playerUnsitEvent.canBeCancelled()) {
      return false;
    }
    sitData.sitting = false;
    player.leaveVehicle();
    sitData.mountedEntity.remove();
    player.setSneaking(false);
    this.occupiedBlocks.remove(sitData.occupiedblock);
    this.sittingPlayers.remove(player);
    if (teleport) {
      player.teleport(playerUnsitEvent.getTeleportLocation().clone());
    }
    if (this.plugin.getChairsConfig().msgEnabled()) {
      player.sendMessage(legacy(this.plugin.getChairsConfig().msgSitLeave()));
    }
    return true;
  }

  protected static class SitData {

    protected final Location teleportBack;
    protected final Block occupiedblock;
    protected final Entity mountedEntity;
    protected boolean sitting;

    public SitData(
        final Location teleportBack,
        final Block occupiedBlock,
        final Entity mountedEntity,
        final boolean sitting
    ) {
      this.teleportBack = teleportBack;
      this.occupiedblock = occupiedBlock;
      this.mountedEntity = mountedEntity;
      this.sitting = sitting;
    }

  }

}
