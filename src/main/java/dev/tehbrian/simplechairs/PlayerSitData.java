package dev.tehbrian.simplechairs;

import dev.tehbrian.simplechairs.api.PlayerChairSitEvent;
import dev.tehbrian.simplechairs.api.PlayerChairUnsitEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public final class PlayerSitData {

    private final SimpleChairs plugin;

    private final NamespacedKey sitDisabledKey;

    private final Map<Player, SitData> sittingPlayers = new HashMap<>();
    private final Map<Block, Player> occupiedBlocks = new HashMap<>();

    public PlayerSitData(final SimpleChairs plugin) {
        this.plugin = plugin;
        this.sitDisabledKey = new NamespacedKey(plugin, "SitDisabled");
    }

    public void disableSitting(final Player player) {
        player.getPersistentDataContainer().set(this.sitDisabledKey, PersistentDataType.BYTE, (byte) 1);
    }

    public void enableSitting(final Player player) {
        player.getPersistentDataContainer().remove(this.sitDisabledKey);
    }

    public boolean isSittingDisabled(final Player player) {
        return player
                .getPersistentDataContainer()
                .getOrDefault(this.sitDisabledKey, PersistentDataType.BYTE, (byte) 0) != 0;
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

    public boolean sitPlayer(final Player player, final Block blocktooccupy, Location sitlocation) {
        final PlayerChairSitEvent playerSitLocation = new PlayerChairSitEvent(player, sitlocation.clone());
        Bukkit.getPluginManager().callEvent(playerSitLocation);
        if (playerSitLocation.isCancelled()) {
            return false;
        }
        sitlocation = playerSitLocation.getSitLocation().clone();
        if (this.plugin.getChairsConfig().msgEnabled()) {
            player.sendMessage(LegacyFormatting.on(this.plugin.getChairsConfig().msgSitEnter()));
        }
        final Entity chairEntity = this.plugin.getSitUtils().spawnChairEntity(sitlocation);
        SitData sitData = null;
        switch (this.plugin.getChairsConfig().sitChairEntityType()) {
            case ARROW -> {
                final int arrowResitInterval = this.plugin.getChairsConfig().sitArrowResitInterval();
                sitData = new SitData(
                        chairEntity, player.getLocation(), blocktooccupy,
                        Bukkit
                                .getScheduler()
                                .scheduleSyncRepeatingTask(
                                        this.plugin,
                                        () -> this.resitPlayer(player),
                                        arrowResitInterval,
                                        arrowResitInterval
                                )
                );
            }
            case ARMOR_STAND -> sitData = new SitData(chairEntity, player.getLocation(), blocktooccupy, -1);
        }
        player.teleport(sitlocation);
        chairEntity.addPassenger(player);
        this.sittingPlayers.put(player, sitData);
        this.occupiedBlocks.put(blocktooccupy, player);
        sitData.sitting = true;
        return true;
    }

    public void resitPlayer(final Player player) {
        final SitData sitData = this.sittingPlayers.get(player);
        sitData.sitting = false;
        final Entity oldEntity = sitData.entity;
        final Entity chairEntity = this.plugin.getSitUtils().spawnChairEntity(oldEntity.getLocation());
        chairEntity.addPassenger(player);
        sitData.entity = chairEntity;
        oldEntity.remove();
        sitData.sitting = true;
    }

    public boolean unsitPlayer(final Player player) {
        return this.unsitPlayer(player, true, true);
    }

    public void unsitPlayerForce(final Player player, final boolean teleport) {
        this.unsitPlayer(player, false, teleport);
    }

    private boolean unsitPlayer(final Player player, final boolean canCancel, final boolean teleport) {
        final SitData sitData = this.sittingPlayers.get(player);
        final PlayerChairUnsitEvent playerUnsitEvent = new PlayerChairUnsitEvent(player, sitData.teleportBackLocation.clone(), canCancel);
        Bukkit.getPluginManager().callEvent(playerUnsitEvent);
        if (playerUnsitEvent.isCancelled() && playerUnsitEvent.canBeCancelled()) {
            return false;
        }
        sitData.sitting = false;
        player.leaveVehicle();
        sitData.entity.remove();
        player.setSneaking(false);
        this.occupiedBlocks.remove(sitData.occupiedBlock);
        if (sitData.resitTaskId != -1) {
            Bukkit.getScheduler().cancelTask(sitData.resitTaskId);
        }
        this.sittingPlayers.remove(player);
        if (teleport) {
            player.teleport(playerUnsitEvent.getTeleportLocation().clone());
        }
        if (this.plugin.getChairsConfig().msgEnabled()) {
            player.sendMessage(LegacyFormatting.on(this.plugin.getChairsConfig().msgSitLeave()));
        }
        return true;
    }

    protected static class SitData {

        protected final Location teleportBackLocation;
        protected final Block occupiedBlock;
        protected final int resitTaskId;

        protected boolean sitting;
        protected Entity entity;

        public SitData(final Entity arrow, final Location teleportLocation, final Block block, final int resitTaskId) {
            this.entity = arrow;
            this.teleportBackLocation = teleportLocation;
            this.occupiedBlock = block;
            this.resitTaskId = resitTaskId;
        }

    }

}
