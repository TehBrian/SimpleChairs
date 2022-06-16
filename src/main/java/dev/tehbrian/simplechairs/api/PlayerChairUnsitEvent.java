package dev.tehbrian.simplechairs.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class PlayerChairUnsitEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final boolean canBeCancelled;
    private boolean cancelled = false;
    private Location unsitLocation;

    public PlayerChairUnsitEvent(final Player who, final Location unsitLocation, final boolean canBeCancelled) {
        super(who);
        this.unsitLocation = unsitLocation;
        this.canBeCancelled = canBeCancelled;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public boolean canBeCancelled() {
        return this.canBeCancelled;
    }

    public Location getTeleportLocation() {
        return this.unsitLocation.clone();
    }

    public void setTeleportLocation(final Location location) {
        this.unsitLocation = location.clone();
    }

    @Override
    public @NonNull HandlerList getHandlers() {
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
