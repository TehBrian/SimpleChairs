package dev.tehbrian.simplechairs.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerChairUnsitEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private boolean canbecancelled = true;
    private Location unsitLocation;

    public PlayerChairUnsitEvent(final Player who, final Location unsitLocation, final boolean canbecancelled) {
        super(who);
        this.unsitLocation = unsitLocation;
        this.canbecancelled = canbecancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean canBeCancelled() {
        return this.canbecancelled;
    }

    public Location getTeleportLocation() {
        return this.unsitLocation.clone();
    }

    public void setTeleportLocation(final Location location) {
        this.unsitLocation = location.clone();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
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
