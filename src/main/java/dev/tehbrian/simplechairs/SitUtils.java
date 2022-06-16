package dev.tehbrian.simplechairs;

import dev.tehbrian.simplechairs.config.ChairsConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Stairs.Shape;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.text.MessageFormat;

public class SitUtils {

    protected final SimpleChairs plugin;
    protected final ChairsConfig config;
    protected final PlayerSitData sitdata;

    public SitUtils(final SimpleChairs plugin) {
        this.plugin = plugin;
        this.config = plugin.getChairsConfig();
        this.sitdata = plugin.getPlayerSitData();
    }

    public Entity spawnChairEntity(Location location) {
        switch (this.config.sitChairEntityType) {
            case ARROW: {
                final Arrow arrow = location.getWorld().spawnArrow(location, new Vector(0, 1, 0), 0, 0);
                arrow.setGravity(false);
                arrow.setInvulnerable(true);
                arrow.setPickupStatus(PickupStatus.DISALLOWED);
                return arrow;
            }
            case ARMOR_STAND: {
                location = location.clone().add(0, 0.4, 0);
                return location.getWorld().spawn(
                        location, ArmorStand.class, armorstand -> {
                            armorstand.setGravity(false);
                            armorstand.setInvulnerable(true);
                            armorstand.setMarker(true);
                            armorstand.setVisible(false);
                        }
                );
            }
            default: {
                throw new IllegalArgumentException("Unknown sit chair entity type " + this.config.sitChairEntityType);
            }
        }
    }

    protected boolean canSitGeneric(final Player player, final Block block) {

        if (player.isSneaking()) {
            return false;
        }
        if (!player.hasPermission("chairs.sit")) {
            return false;
        }

        final World world = player.getWorld();
        if (this.config.sitDisabledWorlds.contains(world.getName())) {
            return false;
        }
        if (!world.equals(block.getWorld())) {
            return false;
        }
        if ((this.config.sitMaxDistance > 0) && (player.getLocation().distance(block
                .getLocation()
                .add(0.5, 0, 0.5)) > this.config.sitMaxDistance)) {
            return false;
        }
        if (this.config.sitRequireEmptyHand && (player.getInventory().getItemInMainHand().getType() != Material.AIR)) {
            return false;
        }

        if (this.sitdata.isSittingDisabled(player)) {
            return false;
        }
        if (this.sitdata.isSitting(player)) {
            return false;
        }
        if (this.sitdata.isBlockOccupied(block)) {
            return false;
        }

        return true;
    }

    public Location calculateSitLocation(final Player player, final Block block) {

        if (!this.canSitGeneric(player, block)) {
            return null;
        }

        final BlockData blockdata = block.getBlockData();
        float yaw = player.getLocation().getYaw();
        Double sitHeight = null;

        if ((blockdata instanceof Stairs) && this.config.stairsEnabled) {
            sitHeight = this.config.stairsHeight;
            final Stairs stairs = (Stairs) blockdata;
            if (!isStairsSittable(stairs)) {
                return null;
            }
            final BlockFace ascendingFacing = stairs.getFacing();
            if (this.config.stairsAutoRotate) {
                switch (ascendingFacing.getOppositeFace()) {
                    case NORTH: {
                        yaw = 180;
                        break;
                    }
                    case EAST: {
                        yaw = -90;
                        break;
                    }
                    case SOUTH: {
                        yaw = 0;
                        break;
                    }
                    case WEST: {
                        yaw = 90;
                        break;
                    }
                    default: {
                    }
                }
            }
            if (this.config.stairsMaxWidth > 0) {
                final BlockFace facingLeft = rotL(ascendingFacing);
                final BlockFace facingRight = rotR(ascendingFacing);
                final int widthLeft = this.calculateStairsWidth(ascendingFacing, block, facingLeft, this.config.stairsMaxWidth);
                final int widthRight = this.calculateStairsWidth(ascendingFacing, block, facingRight, this.config.stairsMaxWidth);
                if ((widthLeft + widthRight + 1) > this.config.stairsMaxWidth) {
                    return null;
                }
                if (this.config.stairsSpecialEndEnabled) {
                    boolean specialEndCheckSuccess = false;
                    final Block blockLeft = block.getRelative(facingLeft, widthLeft + 1);
                    final Block blockRight = block.getRelative(facingRight, widthRight + 1);
                    if (
                            this.config.stairsSpecialEndSign &&
                                    isStairsEndingSign(facingLeft, blockLeft) &&
                                    isStairsEndingSign(facingRight, blockRight)
                    ) {
                        specialEndCheckSuccess = true;
                    }
                    if (
                            this.config.stairsSpecialEndCornerStairs && (
                                    isStairsEndingCornerStairs(facingLeft, Stairs.Shape.INNER_RIGHT, blockLeft) ||
                                            isStairsEndingCornerStairs(ascendingFacing, Stairs.Shape.INNER_LEFT, blockLeft)
                            ) && (
                                    isStairsEndingCornerStairs(facingRight, Stairs.Shape.INNER_LEFT, blockRight) ||
                                            isStairsEndingCornerStairs(ascendingFacing, Stairs.Shape.INNER_RIGHT, blockRight)
                            )
                    ) {
                        specialEndCheckSuccess = true;
                    }
                    if (!specialEndCheckSuccess) {
                        return null;
                    }
                }
            }
        }

        if (sitHeight == null) {
            sitHeight = this.config.additionalChairs.get(blockdata.getMaterial());
            if (sitHeight == null) {
                return null;
            }
        }

        final Location plocation = block.getLocation();
        plocation.setYaw(yaw);
        plocation.add(0.5D, (sitHeight - 0.5D), 0.5D);
        return plocation;
    }

    protected static final boolean isStairsSittable(final Stairs stairs) {
        return (stairs.getHalf() == Half.BOTTOM) && (stairs.getShape() == Shape.STRAIGHT);
    }

    protected static boolean isStairsEndingSign(final BlockFace expectedFacing, final Block block) {
        final BlockData blockdata = block.getBlockData();
        if (blockdata instanceof WallSign) {
            return expectedFacing == ((WallSign) blockdata).getFacing();
        }
        return false;
    }

    protected static boolean isStairsEndingCornerStairs(
            final BlockFace expectedFacing,
            final Stairs.Shape expectedShape,
            final Block block
    ) {
        final BlockData blockdata = block.getBlockData();
        if (blockdata instanceof Stairs) {
            final Stairs stairs = (Stairs) blockdata;
            return (stairs.getHalf() == Half.BOTTOM) && (stairs.getFacing() == expectedFacing) && (stairs.getShape() == expectedShape);
        }
        return false;
    }

    protected int calculateStairsWidth(final BlockFace expectedFace, Block block, final BlockFace searchFace, final int limit) {
        for (int i = 0; i < limit; i++) {
            block = block.getRelative(searchFace);
            final BlockData blockdata = block.getBlockData();
            if (!(blockdata instanceof Stairs)) {
                return i;
            }
            final Stairs stairs = (Stairs) blockdata;
            if (!isStairsSittable(stairs) || (stairs.getFacing() != expectedFace)) {
                return i;
            }
        }
        return limit;
    }

    protected static BlockFace rotL(final BlockFace face) {
        switch (face) {
            case NORTH: {
                return BlockFace.WEST;
            }
            case WEST: {
                return BlockFace.SOUTH;
            }
            case SOUTH: {
                return BlockFace.EAST;
            }
            case EAST: {
                return BlockFace.NORTH;
            }
            default: {
                throw new IllegalArgumentException(MessageFormat.format("Cant rotate blockface {0}", face));
            }
        }
    }

    protected static BlockFace rotR(final BlockFace face) {
        switch (face) {
            case NORTH: {
                return BlockFace.EAST;
            }
            case EAST: {
                return BlockFace.SOUTH;
            }
            case SOUTH: {
                return BlockFace.WEST;
            }
            case WEST: {
                return BlockFace.NORTH;
            }
            default: {
                throw new IllegalArgumentException(MessageFormat.format("Cant rotate blockface {0}", face));
            }
        }
    }

//    private boolean checkFrame(Block block, BlockFace face, Player player) {
//        // Go through the blocks next to the clicked block and check if are signs on the end.
//
//        for (int i = 1; i <= plugin.maxChairWidth + 2; i++) {
//            Block relative = block.getRelative(face, i);
//            if (checkDirection(block, relative)) {
//                continue;
//            }
//            if (relative.getType().equals(Material.AIR)) {
//                int x = relative.getLocation().getBlockX();
//                int y = relative.getLocation().getBlockY();
//                int z = relative.getLocation().getBlockZ();
//                for (Entity e : player.getNearbyEntities(plugin.maxDistance, plugin.maxDistance, plugin.maxDistance)) {
//                    if (e instanceof ItemFrame && plugin.validSigns.contains(Material.ITEM_FRAME)) {
//                        int x2 = e.getLocation().getBlockX();
//                        int y2 = e.getLocation().getBlockY();
//                        int z2 = e.getLocation().getBlockZ();
//                        if (x == x2 && y == y2 && z == z2) {
//                            return true;
//                        }
//                    }
//                }
//                return false;
//            } else {
//                return false;
//            }
//        }
//        return false;
//    }
}
