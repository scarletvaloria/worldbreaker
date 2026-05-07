package net.scarletvaloria.worldbreaker.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    private static final Identifier BLUR_SHADER = Identifier.of("minecraft", "shaders/post/blur.json");

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.player == null || client.world == null) {
            return;
        }

        boolean effectActive = false;

        for (StatusEffectInstance instance : client.player.getStatusEffects()) {
            var keyOpt = instance.getEffectType().getKey();
            if (keyOpt.isPresent()) {
                Identifier id = keyOpt.get().getValue();
                if (id.getNamespace().equals("worldbreaker") && id.getPath().equals("concussed")) {
                    effectActive = true;
                    break;
                }
            }
        }

        GameRendererAccessor accessor = (GameRendererAccessor) client.gameRenderer;
        boolean hasShader = accessor.worldbreaker$getPostProcessor() != null;

        if (effectActive && !hasShader) {
            accessor.worldbreaker$callLoadPostProcessor(BLUR_SHADER);
        } else if (!effectActive && hasShader) {
            String shaderName = accessor.worldbreaker$getPostProcessor().getName();
            if (shaderName != null && shaderName.contains("blur")) {
                client.gameRenderer.disablePostProcessor();
            }
        }
    }
}
