package dev.hybridbridging;

import net.minecraft.block.BlockItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Raycaster {
    private Raycaster() {}

    public static BlockHitResult raycast(PlayerEntity player, World world, double reach) {
        if (player == null || world == null || !Double.isFinite(reach) || reach <= 0.0d) return null;
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d end = start.add(player.getRotationVector().multiply(reach));
        HitResult hit = world.raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK ? (BlockHitResult) hit : null;
    }

    public static BlockHitResult tryFallbackPlacement(PlayerEntity player, World world, BlockPos origin,
            Hand hand, ItemStack stack, Vec3d movementVector, boolean diagonalAssistance,
            boolean edgeAssistance, BridgingConfig config) {
        if (player == null || world == null || origin == null || hand == null || stack == null
                || stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)
                || config == null || player.isSpectator()) return null;

        int radius = Math.max(1, Math.min(5, config.fallbackSearchRadius));
        double tolerance = Double.isFinite(config.placementTolerance)
                ? Math.max(0.01d, Math.min(1.5d, config.placementTolerance)) : 0.25d;
        double reach = player.getBlockInteractionRange();
        if (!Double.isFinite(reach) || reach <= 0.0d) return null;

        Vec3d rayStart = player.getCameraPosVec(1.0f);
        Vec3d rayEnd = rayStart.add(player.getRotationVector().multiply(reach));
        Box playerBounds = player.getBoundingBox();
        List<PlacementCandidate> candidates = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos supportPos = origin.add(dx, dy, dz);
                    BlockState supportState = world.getBlockState(supportPos);
                    if (supportState.isAir()) continue;

                    for (Direction face : Direction.values()) {
                        if (!supportState.isSideSolidFullSquare(world, supportPos, face)) continue;
                        BlockPos placePos = supportPos.offset(face);
                        if (!world.getBlockState(placePos).isReplaceable()) continue;

                        Box destinationBounds = new Box(placePos.getX(), placePos.getY(), placePos.getZ(),
                                placePos.getX() + 1.0d, placePos.getY() + 1.0d, placePos.getZ() + 1.0d);
                        if (playerBounds.intersects(destinationBounds)) continue;
                        if (!player.canInteractWithBlockAt(supportPos, 0.0d)) continue;

                        Vec3d hitPoint = Vec3d.ofCenter(supportPos).add(face.getOffsetX() * 0.5d,
                                face.getOffsetY() * 0.5d, face.getOffsetZ() * 0.5d);
                        if (hitPoint.squaredDistanceTo(rayStart) > reach * reach
                                || PlacementLogic.distanceToSegment(hitPoint, rayStart, rayEnd) > tolerance) continue;

                        BlockHitResult hitResult = new BlockHitResult(hitPoint, face, supportPos, false);
                        ItemPlacementContext context = new ItemPlacementContext(player, hand, stack, hitResult);
                        if (!context.canPlace() || !context.getBlockPos().equals(placePos)
                                || blockItem.getBlock().getPlacementState(context) == null) continue;

                        double score = PlacementLogic.scoreCandidate(face, movementVector,
                                diagonalAssistance, edgeAssistance, hitPoint, rayStart, rayEnd, tolerance);
                        candidates.add(new PlacementCandidate(hitResult, score));
                    }
                }
            }
        }

        return candidates.stream().max(Comparator.comparingDouble(PlacementCandidate::score))
                .map(PlacementCandidate::hitResult).orElse(null);
    }

    private record PlacementCandidate(BlockHitResult hitResult, double score) {}
}
