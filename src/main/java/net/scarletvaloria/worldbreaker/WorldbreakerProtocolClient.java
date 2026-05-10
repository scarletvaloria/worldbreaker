package net.scarletvaloria.worldbreaker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.scarletvaloria.worldbreaker.index.ModComponents;
import net.scarletvaloria.worldbreaker.index.ModParticles;
import net.scarletvaloria.worldbreaker.index.WorldbreakerClientState;
import net.scarletvaloria.worldbreaker.item.TomahawkItem;
import net.scarletvaloria.worldbreaker.network.TomahawkSyncPacket;

public class WorldbreakerProtocolClient implements ClientModInitializer {

    private boolean wasActive = false;

    @Override
    public void onInitializeClient() {

        ClientPlayNetworking.registerGlobalReceiver(
                TomahawkSyncPacket.ID,
                (payload, ctx) -> {
                    ctx.client().execute(() ->
                            WorldbreakerClientState.set(payload.charges())
                    );
                }
        );

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

            if (WorldbreakerClientState.flashIntensity <= 0) return;

            int alpha = (int)(WorldbreakerClientState.flashIntensity * 255);

            drawContext.fill(
                    0, 0,
                    drawContext.getScaledWindowWidth(),
                    drawContext.getScaledWindowHeight(),
                    (alpha << 24) | 0xFFFFFF
            );

            WorldbreakerClientState.flashIntensity -= 0.03f;
        });
    }
}