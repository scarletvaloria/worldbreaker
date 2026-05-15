package net.scarletvaloria.worldbreaker.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.scarletvaloria.worldbreaker.item.TomahawkItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFallMixin {

    @Inject(method = "fall", at = @At("TAIL"))
    private void worldbreaker$onFall(double heightDifference, boolean onGround,
                                     net.minecraft.block.BlockState state,
                                     net.minecraft.util.math.BlockPos pos,
                                     CallbackInfo ci) {

        LivingEntity entity = (LivingEntity)(Object)this;

        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (!player.isAlive()) return;

        if (!player.isOnGround()) return;

        float fallDistance = player.fallDistance;

        if (fallDistance < 8.0f) return;

        if (player.isFallFlying()) return;
        if (player.getAbilities().flying) return;
    }
}