package dev.tehbrian.simplechairs.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerChairSitEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private Location sitLocation;

    public PlayerChairSitEvent(final Player who, final Location sitLocation) {
        super(who);
        this.sitLocation = sitLocation;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Location getSitLocation() {
        return this.sitLocation.clone();
    }

    public void setSitLocation(final Location location) {
        this.sitLocation = location.clone();
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
