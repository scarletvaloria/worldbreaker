package net.scarletvaloria.worldbreaker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.*;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.EntityType;
import net.scarletvaloria.worldbreaker.index.ModComponents;
import net.scarletvaloria.worldbreaker.index.ModModelLayers;
import net.scarletvaloria.worldbreaker.index.ModParticles;
import net.scarletvaloria.worldbreaker.index.WorldbreakerClientState;
import net.scarletvaloria.worldbreaker.model.WorldbreakerFeatureRenderer;
import net.scarletvaloria.worldbreaker.model.WorldbreakerWorldRenderer;
import net.scarletvaloria.worldbreaker.client.model.WorldbreakerModel;

public class WorldbreakerProtocolClient implements ClientModInitializer {

    private boolean wasActive = false;

    @Override
    public void onInitializeClient() {

        ModParticles.registerParticlesClient();

        registerClientTicks();
        registerHud();
    }

    private void registerClientTicks() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            boolean active = ModComponents.FORM_STATE.get(client.player).isActive();

            if (active != wasActive) {
                WorldbreakerClientState.triggerFlash();
                wasActive = active;
            }
        });
    }

    private void registerHud() {
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            if (WorldbreakerClientState.flashIntensity > 0) {

                int alpha = (int)(WorldbreakerClientState.flashIntensity * 255);

                drawContext.fill(
                        0, 0,
                        drawContext.getScaledWindowWidth(),
                        drawContext.getScaledWindowHeight(),
                        (alpha << 24) | 0xFFFFFF
                );

                WorldbreakerClientState.flashIntensity -= 0.03f;
            }
        });
    }
}