package net.scarletvaloria.worldbreaker.mixin;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor("postProcessor")
    PostEffectProcessor worldbreaker$getPostProcessor();

    @Invoker("loadPostProcessor")
    void worldbreaker$callLoadPostProcessor(Identifier id);
}
