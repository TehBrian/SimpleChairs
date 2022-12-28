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

public final class SitUtils {

  private final SimpleChairsPlugin plugin;
  private final ChairsConfig config;

  public SitUtils(final SimpleChairsPlugin plugin) {
    this.plugin = plugin;
    this.config = plugin.getChairsConfig();
  }

  private static boolean isStairsSittable(final Stairs stairs) {
    return (stairs.getHalf() == Half.BOTTOM) && (stairs.getShape() == Shape.STRAIGHT);
  }

  private static boolean isStairsEndingSign(final BlockFace expectedFacing, final Block block) {
    final BlockData blockdata = block.getBlockData();
    if (blockdata instanceof WallSign) {
      return expectedFacing == ((WallSign) blockdata).getFacing();
    }
    return false;
  }

  private static boolean isStairsEndingCornerStairs(
      final BlockFace expectedFacing,
      final Stairs.Shape expectedShape,
      final Block block
  ) {
    final BlockData blockdata = block.getBlockData();
    if (blockdata instanceof final Stairs stairs) {
      return (stairs.getHalf() == Half.BOTTOM)
          && (stairs.getFacing() == expectedFacing)
          && (stairs.getShape() == expectedShape);
    }
    return false;
  }

  private static BlockFace rotL(final BlockFace face) {
    switch (face) {
      case NORTH -> {
        return BlockFace.WEST;
      }
      case WEST -> {
        return BlockFace.SOUTH;
      }
      case SOUTH -> {
        return BlockFace.EAST;
      }
      case EAST -> {
        return BlockFace.NORTH;
      }
      default -> throw new IllegalArgumentException(MessageFormat.format("Cant rotate block face {0}", face));
    }
  }

  private static BlockFace rotR(final BlockFace face) {
    switch (face) {
      case NORTH -> {
        return BlockFace.EAST;
      }
      case EAST -> {
        return BlockFace.SOUTH;
      }
      case SOUTH -> {
        return BlockFace.WEST;
      }
      case WEST -> {
        return BlockFace.NORTH;
      }
      default -> throw new IllegalArgumentException(MessageFormat.format("Cant rotate block face {0}", face));
    }
  }

  public Entity spawnChairEntity(final Location location) {
    switch (this.config.sitChairEntityType()) {
      case ARROW -> {
        final Arrow arrow = location.getWorld().spawnArrow(location, new Vector(0, 1, 0), 0, 0);
        arrow.setGravity(false);
        arrow.setInvulnerable(true);
        arrow.setPickupStatus(PickupStatus.DISALLOWED);
        return arrow;
      }
      case ARMOR_STAND -> {
        final Location adjustedLoc = location.clone().add(0, 0.4, 0);
        return adjustedLoc.getWorld().spawn(
            adjustedLoc, ArmorStand.class, armorStand -> {
              armorStand.setGravity(false);
              armorStand.setInvulnerable(true);
              armorStand.setMarker(true);
              armorStand.setVisible(false);
            }
        );
      }
      default ->
          throw new IllegalArgumentException("Unknown sit chair entity type " + this.config.sitChairEntityType());
    }
  }

  private boolean canSitGeneric(final Player player, final Block block) {
    if (player.isSneaking()) {
      return false;
    }
    if (!player.hasPermission("chairs.sit")) {
      return false;
    }

    final World world = player.getWorld();
    if (this.config.sitDisabledWorlds().contains(world.getName())) {
      return false;
    }
    if (!world.equals(block.getWorld())) {
      return false;
    }
    if ((this.config.sitMaxDistance() > 0)
        && (player.getLocation().distance(block.getLocation().add(0.5, 0, 0.5)) > this.config.sitMaxDistance())) {
      return false;
    }
    if (this.config.sitRequireEmptyHand() && (player.getInventory().getItemInMainHand().getType() != Material.AIR)) {
      return false;
    }

    final PlayerSitData sitData = this.plugin.getPlayerSitData();
    if (sitData.isSittingDisabled(player)) {
      return false;
    }
    if (sitData.isSitting(player)) {
      return false;
    }
    return !sitData.isBlockOccupied(block);
  }

  public Location calculateSitLocation(final Player player, final Block block) {
    if (!this.canSitGeneric(player, block)) {
      return null;
    }

    final BlockData blockData = block.getBlockData();
    float yaw = player.getLocation().getYaw();
    Double sitHeight = null;

    if ((blockData instanceof final Stairs stairs) && this.config.sitStairsEnabled()) {
      sitHeight = this.config.sitStairsHeight();
      if (!isStairsSittable(stairs)) {
        return null;
      }
      final BlockFace ascendingFacing = stairs.getFacing();

      if (this.config.sitStairsRotatePlayer()) {
        yaw = switch (ascendingFacing.getOppositeFace()) {
          case NORTH -> 180;
          case EAST -> -90;
          case SOUTH -> 0;
          case WEST -> 90;
          default -> yaw;
        };
      }

      if (this.config.sitStairsMaxWidth() > 0) {
        final BlockFace facingLeft = rotL(ascendingFacing);
        final BlockFace facingRight = rotR(ascendingFacing);
        final int widthLeft = this.calculateStairsWidth(
            ascendingFacing,
            block,
            facingLeft,
            this.config.sitStairsMaxWidth()
        );
        final int widthRight = this.calculateStairsWidth(
            ascendingFacing,
            block,
            facingRight,
            this.config.sitStairsMaxWidth()
        );
        if ((widthLeft + widthRight + 1) > this.config.sitStairsMaxWidth()) {
          return null;
        }

        if (this.config.sitStairsSpecialEndEnabled()) {
          boolean specialEndCheckSuccess = false;
          final Block blockLeft = block.getRelative(facingLeft, widthLeft + 1);
          final Block blockRight = block.getRelative(facingRight, widthRight + 1);

          if (this.config.sitStairsSpecialEndSign()
              && isStairsEndingSign(facingLeft, blockLeft)
              && isStairsEndingSign(facingRight, blockRight)) {
            specialEndCheckSuccess = true;
          }

          if (this.config.sitStairsSpecialEndCornerStairs()
              && (isStairsEndingCornerStairs(facingLeft, Stairs.Shape.INNER_RIGHT, blockLeft)
              || isStairsEndingCornerStairs(ascendingFacing, Stairs.Shape.INNER_LEFT, blockLeft))
              && (isStairsEndingCornerStairs(facingRight, Stairs.Shape.INNER_LEFT, blockRight)
              || isStairsEndingCornerStairs(ascendingFacing, Stairs.Shape.INNER_RIGHT, blockRight))) {
            specialEndCheckSuccess = true;
          }

          if (!specialEndCheckSuccess) {
            return null;
          }
        }
      }
    }

    if (sitHeight == null) {
      sitHeight = this.config.sitAdditionalBlocks().get(blockData.getMaterial());
      if (sitHeight == null) {
        return null;
      }
    }

    final Location pLocation = block.getLocation();
    pLocation.setYaw(yaw);
    pLocation.add(0.5D, (sitHeight - 0.5D), 0.5D);
    return pLocation;
  }

  private int calculateStairsWidth(
      final BlockFace expectedFace,
      final Block startBlock,
      final BlockFace searchFace,
      final int limit
  ) {
    Block currentBlock = startBlock;
    for (int i = 0; i < limit; i++) {
      currentBlock = currentBlock.getRelative(searchFace);
      final BlockData blockData = currentBlock.getBlockData();
      if (!(blockData instanceof final Stairs stairs)) {
        return i;
      }
      if (!isStairsSittable(stairs) || (stairs.getFacing() != expectedFace)) {
        return i;
      }
    }
    return limit;
  }
}
