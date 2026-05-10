package net.scarletvaloria.worldbreaker.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Identifier;
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;
import net.scarletvaloria.worldbreaker.index.ModComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(method = "getTexture", at = @At("HEAD"), cancellable = true)
    private void worldbreaker$skinOverride(AbstractClientPlayerEntity player, CallbackInfoReturnable<Identifier> cir) {

        if (ModComponents.FORM_STATE.get(player).isActive()) {
            cir.setReturnValue(
                    Identifier.of(WorldbreakerProtocol.MOD_ID, "textures/entity/worldbreaker.png")
            );
        }
    }
}