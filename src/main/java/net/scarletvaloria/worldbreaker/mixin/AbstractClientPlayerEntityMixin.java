package net.scarletvaloria.worldbreaker.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.scarletvaloria.worldbreaker.index.ModComponents;
import net.scarletvaloria.worldbreaker.model.WorldbreakerRenderState;
import net.scarletvaloria.worldbreaker.model.WorldbreakerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {

    @Inject(method = "getSkinTextures", at = @At("HEAD"), cancellable = true)
    private void worldbreaker$override(CallbackInfoReturnable<SkinTextures> cir) {

        AbstractClientPlayerEntity self = (AbstractClientPlayerEntity)(Object)this;

        if (ModComponents.FORM_STATE.get(self).isActive()
                || WorldbreakerRenderState.FORCE_WORLDBREAKER_SKIN) {

            cir.setReturnValue(
                    new SkinTextures(
                            WorldbreakerSkin.WORLDBREAKER,
                            null,
                            null,
                            null,
                            SkinTextures.Model.WIDE,
                            true
                    )
            );
        }
    }
}
