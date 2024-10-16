package dev.tehbrian.simplechairs.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerChairUnsitEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private boolean cancelled = false;
	private Location retreat;

	public PlayerChairUnsitEvent(final Player who, final Location retreat) {
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

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(final boolean cancelled) {
		this.cancelled = cancelled;
	}

}
