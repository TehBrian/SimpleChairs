package dev.tehbrian.simplechairs;

import java.util.HashMap;
import java.util.Map;

import dev.tehbrian.simplechairs.api.PlayerChairSitEvent;
import dev.tehbrian.simplechairs.api.PlayerChairUnsitEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class PlayerSitData {

    protected final SimpleChairs plugin;

    protected final NamespacedKey sitDisabledKey;

    protected final Map<Player, SitData> sittingPlayers = new HashMap<>();
    protected final Map<Block, Player> occupiedBlocks = new HashMap<>();

    public PlayerSitData(final SimpleChairs plugin) {
        this.plugin = plugin;
        this.sitDisabledKey = new NamespacedKey(plugin, "SitDisabled");
    }

    public void disableSitting(final Player player) {
        player.getPersistentDataContainer().set(sitDisabledKey, PersistentDataType.BYTE, Byte.valueOf((byte) 1));
    }

    public void enableSitting(final Player player) {
        player.getPersistentDataContainer().remove(sitDisabledKey);
    }

    public boolean isSittingDisabled(final Player player) {
        return player.getPersistentDataContainer().getOrDefault(sitDisabledKey, PersistentDataType.BYTE, Byte.valueOf((byte) 0)).byteValue() != 0;
    }

    public boolean isSitting(final Player player) {
        final SitData sitdata = sittingPlayers.get(player);
        return (sitdata != null) && sitdata.sitting;
    }

    public boolean isBlockOccupied(final Block block) {
        return occupiedBlocks.containsKey(block);
    }

    public Player getPlayerOnChair(final Block chair) {
        return occupiedBlocks.get(chair);
    }

    public boolean sitPlayer(final Player player, final Block blocktooccupy, Location sitlocation) {
        final PlayerChairSitEvent playersitevent = new PlayerChairSitEvent(player, sitlocation.clone());
        Bukkit.getPluginManager().callEvent(playersitevent);
        if (playersitevent.isCancelled()) {
            return false;
        }
        sitlocation = playersitevent.getSitLocation().clone();
        if (plugin.getChairsConfig().msgEnabled) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getChairsConfig().msgSitEnter));
        }
        final Entity chairentity = plugin.getSitUtils().spawnChairEntity(sitlocation);
        SitData sitdata = null;
        switch (plugin.getChairsConfig().sitChairEntityType) {
            case ARROW: {
                final int arrowresitinterval = plugin.getChairsConfig().sitArrowResitInterval;
                sitdata = new SitData(
                    chairentity, player.getLocation(), blocktooccupy,
                    Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> resitPlayer(player), arrowresitinterval, arrowresitinterval)
                );
                break;
            }
            case ARMOR_STAND: {
                sitdata = new SitData(chairentity, player.getLocation(), blocktooccupy, -1);
                break;
            }
        }
        player.teleport(sitlocation);
        chairentity.addPassenger(player);
        sittingPlayers.put(player, sitdata);
        occupiedBlocks.put(blocktooccupy, player);
        sitdata.sitting = true;
        return true;
    }

    public void resitPlayer(final Player player) {
        final SitData sitdata = sittingPlayers.get(player);
        sitdata.sitting = false;
        final Entity oldentity = sitdata.entity;
        final Entity chairentity = plugin.getSitUtils().spawnChairEntity(oldentity.getLocation());
        chairentity.addPassenger(player);
        sitdata.entity = chairentity;
        oldentity.remove();
        sitdata.sitting = true;
    }

    public boolean unsitPlayer(final Player player) {
        return unsitPlayer(player, true, true);
    }

    public void unsitPlayerForce(final Player player, final boolean teleport) {
        unsitPlayer(player, false, teleport);
    }

    private boolean unsitPlayer(final Player player, final boolean canCancel, final boolean teleport) {
        final SitData sitdata = sittingPlayers.get(player);
        final PlayerChairUnsitEvent playerunsitevent = new PlayerChairUnsitEvent(player, sitdata.teleportBackLocation.clone(), canCancel);
        Bukkit.getPluginManager().callEvent(playerunsitevent);
        if (playerunsitevent.isCancelled() && playerunsitevent.canBeCancelled()) {
            return false;
        }
        sitdata.sitting = false;
        player.leaveVehicle();
        sitdata.entity.remove();
        player.setSneaking(false);
        occupiedBlocks.remove(sitdata.occupiedBlock);
        if (sitdata.resitTaskId != -1) {
            Bukkit.getScheduler().cancelTask(sitdata.resitTaskId);
        }
        sittingPlayers.remove(player);
        if (teleport) {
            player.teleport(playerunsitevent.getTeleportLocation().clone());
        }
        if (plugin.getChairsConfig().msgEnabled) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getChairsConfig().msgSitLeave));
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
