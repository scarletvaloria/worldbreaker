package net.scarletvaloria.worldbreaker.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.scarletvaloria.worldbreaker.index.ModComponents;
import net.scarletvaloria.worldbreaker.index.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void adjustAMWDAnimation(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (player.isUsingItem() && player.getActiveItem().isOf(ModItems.AMWD) && hand == Hand.MAIN_HAND) {
            matrices.translate(0.2, -0.4, -0.3);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-10));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-10));
        }
    }
}

