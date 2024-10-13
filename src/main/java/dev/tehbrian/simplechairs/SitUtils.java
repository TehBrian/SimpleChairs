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
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

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

	private static boolean isStairsEndingSign(final Block block, final BlockFace expectedFacing) {
		final BlockData blockData = block.getBlockData();
		if (blockData instanceof final WallSign wallSign) {
			return expectedFacing == wallSign.getFacing();
		}
		return false;
	}

	private static boolean isStairsEndingCornerStairs(
			final Block block,
			final BlockFace expectedFacing,
			final Shape expectedShape
	) {
		final BlockData blockData = block.getBlockData();
		if (blockData instanceof final Stairs stairs) {
			return (stairs.getHalf() == Half.BOTTOM)
					&& (stairs.getFacing() == expectedFacing)
					&& (stairs.getShape() == expectedShape);
		}
		return false;
	}

	private static BlockFace rotL(final BlockFace face) {
		return switch (face) {
			case NORTH -> BlockFace.WEST;
			case WEST -> BlockFace.SOUTH;
			case SOUTH -> BlockFace.EAST;
			case EAST -> BlockFace.NORTH;
			default -> throw new IllegalArgumentException("Can't rotate block face " + face);
		};
	}

	private static BlockFace rotR(final BlockFace face) {
		return switch (face) {
			case NORTH -> BlockFace.EAST;
			case EAST -> BlockFace.SOUTH;
			case SOUTH -> BlockFace.WEST;
			case WEST -> BlockFace.NORTH;
			default -> throw new IllegalArgumentException("Can't rotate block face " + face);
		};
	}

	private boolean canSitGeneric(final Player player, final Block block) {
		if (player.isSneaking()) {
			return false;
		}
		if (!player.hasPermission(Permissions.SIT)) {
			return false;
		}

		final World world = player.getWorld();
		if (!world.equals(block.getWorld())) {
			return false;
		}
		if (this.config.sitDisabledWorlds().contains(world.getName())) {
			return false;
		}
		if ((this.config.sitMaxDistance() > 0)
				&& (player.getLocation().distanceSquared(block.getLocation().add(0.5, 0, 0.5)) > this.config.sitMaxDistanceSquared())) {
			return false;
		}
		if (this.config.sitRequireEmptyHand() && (player.getInventory().getItemInMainHand().getType() != Material.AIR)) {
			return false;
		}

		final PlayerSitService sitService = this.plugin.getSitService();
		if (sitService.isSittingDisabled(player)) {
			return false;
		}
		if (sitService.isSitting(player)) {
			return false;
		}
		if (sitService.isBlockOccupied(block)) {
			return false;
		}

		return true;
	}

	public @Nullable Location calculatePerch(final Player player, final Block block) {
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
							&& isStairsEndingSign(blockLeft, facingLeft)
							&& isStairsEndingSign(blockRight, facingRight)) {
						specialEndCheckSuccess = true;
					}

					if (this.config.sitStairsSpecialEndCornerStairs()
							&& (isStairsEndingCornerStairs(blockLeft, facingLeft, Stairs.Shape.INNER_RIGHT)
							|| isStairsEndingCornerStairs(blockLeft, ascendingFacing, Stairs.Shape.INNER_LEFT))
							&& (isStairsEndingCornerStairs(blockRight, facingRight, Stairs.Shape.INNER_LEFT)
							|| isStairsEndingCornerStairs(blockRight, ascendingFacing, Stairs.Shape.INNER_RIGHT))) {
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

		final Location perch = block.getLocation();
		perch.setYaw(yaw);
		perch.add(0.5D, sitHeight, 0.5D);
		return perch;
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
