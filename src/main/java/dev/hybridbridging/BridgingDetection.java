package dev.hybridbridging;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class BridgingDetection {
    private static final double MAX_EDGE_LOOK_DISTANCE = 2.5d;
    private static final double MAX_LATERAL_DISTANCE = 0.75d;

    private BridgingDetection() {}

    public static boolean shouldUseEdgeAssistance(PlayerEntity player, World world, BlockPos feetPos, Direction facing) {
        if (player == null || world == null || feetPos == null || facing == null) return false;
        if (!player.isOnGround() && !player.isSneaking()) return false;

        BlockPos supportAhead = feetPos.down().offset(facing);
        BlockState supportState = world.getBlockState(supportAhead);
        if (!supportState.getCollisionShape(world, supportAhead).isEmpty()) return false;

        Vec3d forward = new Vec3d(facing.getOffsetX(), 0.0d, facing.getOffsetZ());
        Vec3d toGap = Vec3d.ofCenter(supportAhead).subtract(player.getPos());
        Vec3d horizontalToGap = new Vec3d(toGap.x, 0.0d, toGap.z);
        double forwardDistance = horizontalToGap.dotProduct(forward);
        double lateralDistance = horizontalToGap.subtract(forward.multiply(forwardDistance)).length();

        return forwardDistance > 0.0d && forwardDistance <= MAX_EDGE_LOOK_DISTANCE
                && lateralDistance <= MAX_LATERAL_DISTANCE;
    }
}
