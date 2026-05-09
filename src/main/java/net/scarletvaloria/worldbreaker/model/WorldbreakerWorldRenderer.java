package net.scarletvaloria.worldbreaker.model;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import net.scarletvaloria.worldbreaker.index.ModComponents;
import net.scarletvaloria.worldbreaker.index.ModModelLayers;
import net.scarletvaloria.worldbreaker.client.model.WorldbreakerModel;

public class WorldbreakerWorldRenderer {

    public static void render(WorldRenderContext context) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        var camera = context.camera();
        MatrixStack matrices = context.matrixStack();

        float tickDelta = context.tickCounter().getTickDelta(true);

        for (var player : client.world.getPlayers()) {

            if (!ModComponents.FORM_STATE.get(player).isActive()) continue;

            matrices.push();

            Vec3d camPos = camera.getPos();
            Vec3d pos = player.getLerpedPos(tickDelta);

            matrices.translate(
                    pos.x - camPos.x,
                    (pos.y - camPos.y) - 1.5F,
                    pos.z - camPos.z
            );

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));

            WorldbreakerModel model = new WorldbreakerModel(
                    client.getEntityModelLoader().getModelPart(ModModelLayers.WORLDBREAKER)
            );

            model.setAngles(
                    player,
                    player.limbAnimator.getPos(),
                    player.limbAnimator.getSpeed(),
                    player.age + tickDelta,
                    player.getHeadYaw(),
                    player.getPitch()
            );

            VertexConsumerProvider.Immediate immediate =
                    client.getBufferBuilders().getEntityVertexConsumers();

            VertexConsumer consumer = immediate.getBuffer(
                    RenderLayer.getEntityCutoutNoCull(
                            Identifier.of("worldbreaker", "textures/entity/worldbreaker.png")
                    )
            );

            model.render(
                    matrices,
                    consumer,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV
            );

            matrices.pop();
        }
    }
}