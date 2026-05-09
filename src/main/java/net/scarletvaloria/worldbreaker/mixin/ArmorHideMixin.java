package net.scarletvaloria.worldbreaker.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.scarletvaloria.worldbreaker.index.ModComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorHideMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cancelArmor(CallbackInfo ci) {

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null &&
                ModComponents.FORM_STATE.get(client.player).isActive()) {
            ci.cancel();
        }
    }
}