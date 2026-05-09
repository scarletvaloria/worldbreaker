package net.scarletvaloria.worldbreaker.model;

import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.scarletvaloria.worldbreaker.client.model.WorldbreakerModel;
import net.scarletvaloria.worldbreaker.index.ModComponents;

public class WorldbreakerFeatureRenderer
        extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {


    private final WorldbreakerModel model;

    public WorldbreakerFeatureRenderer(
            FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context,
            WorldbreakerModel model
    ) {
        super(context);
        this.model = model;
    }


    @Override
    public void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            AbstractClientPlayerEntity player,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch
    ) {
        if (!ModComponents.FORM_STATE.get(player).isActive()) return;

        System.out.println("Worldbreaker feature rendering for " + player.getName().getString());

        VertexConsumer consumer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(
                        Identifier.of("worldbreaker", "textures/entity/worldbreaker.png")
                )
        );

        model.animateModel(player, limbAngle, limbDistance, tickDelta);
        model.setAngles(player, limbAngle, limbDistance, animationProgress, headYaw, headPitch);

        model.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV);
    }
}