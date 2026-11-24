package dev.hybridbridging.mixin;

import dev.hybridbridging.BridgingConfig;
import dev.hybridbridging.BridgingDetection;
import dev.hybridbridging.Raycaster;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Unique
    private boolean hybridBridging$applyingFallback;

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void hybridBridging$interceptPlacement(ClientPlayerEntity player, Hand hand,
            BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (hybridBridging$applyingFallback) return;

        BridgingConfig config = BridgingConfig.get();
        ItemStack stack = player.getStackInHand(hand);
        if (!config.enabled || stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) return;
        if (player.isSpectator() || !player.isAlive()) return;
        if (hitResult == null || hitResult.getType() != HitResult.Type.MISS) return;
        if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) return;

        World world = player.getWorld();
        BlockPos origin = player.getBlockPos().down();
        Direction facing = player.getHorizontalFacing();
        Vec3d movement = new Vec3d(player.getVelocity().x, 0.0d, player.getVelocity().z);
        if (movement.lengthSquared() < 1.0E-4d) {
            movement = new Vec3d(facing.getOffsetX(), 0.0d, facing.getOffsetZ());
        }

        Vec3d normalized = movement.normalize();
        boolean diagonal = config.diagonalAssistance
                && Math.abs(normalized.x) > 0.2d && Math.abs(normalized.z) > 0.2d;
        boolean edge = config.edgeAssistance
                && BridgingDetection.shouldUseEdgeAssistance(player, world, player.getBlockPos(), facing);

        BlockHitResult fallbackHit = Raycaster.tryFallbackPlacement(player, world, origin, hand,
                stack, movement, diagonal, edge, config);
        if (fallbackHit == null) return;

        ClientPlayerInteractionManager manager = (ClientPlayerInteractionManager) (Object) this;
        hybridBridging$applyingFallback = true;
        try {
            cir.setReturnValue(manager.interactBlock(player, hand, fallbackHit));
        } finally {
            hybridBridging$applyingFallback = false;
        }
    }
}
