package net.scarletvaloria.worldbreaker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.scarletvaloria.worldbreaker.index.*;

public class WorldbreakerProtocolClient implements ClientModInitializer {

    private boolean wasActive = false;

    private static final Identifier TEXTURE =
            Identifier.of(
                    WorldbreakerProtocol.MOD_ID,
                    "textures/models/armor/worldbreaker_chestplate.png"
            );

    @Override
    public void onInitializeClient() {

        ModParticles.registerParticlesClient();

        registerClientTicks();
        registerHud();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (WorldbreakerClientState.initialized) return;

            WorldbreakerClientState.initialized = true;
        });
    }


    private void registerClientTicks() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null) return;

            boolean active =
                    ModComponents.FORM_STATE.get(client.player).isActive();

            if (active != wasActive) {
                WorldbreakerClientState.triggerFlash();
                wasActive = active;
            }

            PlayerEntity player = client.player;

            if (ModComponents.FORM_STATE.get(player).isActive()) {

                if (!player.isOnGround()
                        && client.options.sneakKey.isPressed()) {

                    player.setVelocity(
                            player.getVelocity().x,
                            0.05,
                            player.getVelocity().z
                    );
                }

                if (!player.isOnGround()) {

                    float speed = 0.05f;

                    if (client.options.leftKey.isPressed()) {

                        player.addVelocity(
                                player.getRotationVector().z * speed,
                                0,
                                -player.getRotationVector().x * speed
                        );
                    }

                    if (client.options.rightKey.isPressed()) {

                        player.addVelocity(
                                -player.getRotationVector().z * speed,
                                0,
                                player.getRotationVector().x * speed
                        );
                    }
                }
            }
        });
    }

    private void registerHud() {

        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register(
                (drawContext, tickCounter) -> {

                    if (WorldbreakerClientState.flashIntensity <= 0) return;

                    int alpha =
                            (int)(WorldbreakerClientState.flashIntensity * 255);

                    drawContext.fill(
                            0,
                            0,
                            drawContext.getScaledWindowWidth(),
                            drawContext.getScaledWindowHeight(),
                            (alpha << 24) | 0xFFFFFF
                    );

                    WorldbreakerClientState.flashIntensity -= 0.03f;
                }
        );
    }
}