package dev.tehbrian.simplechairs.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerChairUnsitForceEvent extends PlayerEvent {

  private static final HandlerList HANDLERS = new HandlerList();

  private Location retreat;

  public PlayerChairUnsitForceEvent(final Player who, final Location retreat) {
    super(who);
    this.retreat = retreat;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  public Location getRetreat() {
    return this.retreat.clone();
  }

  public void setRetreat(final Location location) {
    this.retreat = location.clone();
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

}
