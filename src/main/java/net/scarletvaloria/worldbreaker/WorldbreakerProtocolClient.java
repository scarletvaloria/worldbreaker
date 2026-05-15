package net.scarletvaloria.worldbreaker;

import net.acoyt.acornlib.api.event.CustomRiptideEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.scarletvaloria.worldbreaker.index.*;
import net.scarletvaloria.worldbreaker.item.TomahawkItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Optional;

public class WorldbreakerProtocolClient implements ClientModInitializer {

    private boolean wasActive = false;

    @Override
    public void onInitializeClient() {

        ModParticles.registerParticlesClient();

        ClientPlayNetworking.registerGlobalReceiver(
                WorldbreakerFlashPacket.ID,
                (payload, context) -> context.client().execute(
                        WorldbreakerClientState::triggerFlash
                )
        );

        ParticleFactoryRegistry.getInstance().register(ModParticles.EXPANDING_RING, ExpandingRingParticle.Factory::new);

        registerInputPackets();
        registerHud();


        CustomRiptideEvent.EVENT.register((player, stack) -> {
            if (stack.getItem() instanceof TomahawkItem) {
                return Optional.of(Identifier.of(
                        WorldbreakerProtocol.MOD_ID,
                        "textures/entity/tomahawk_riptide.png"
                ));
            }
            return Optional.empty();
        });

        ClientTickEvents.END_CLIENT_TICK.register(this::clientTick);
    }

    private void clientTick(MinecraftClient client) {

        if (client.player == null) return;

        PlayerEntity player = client.player;

        boolean active =
                ModComponents.FORM_STATE.get(player).isActive();

        if (active != wasActive) {
            wasActive = active;
        }

        if (!active) return;

        handleAirSteering(client, player);
    }

    private void handleAirSteering(MinecraftClient client, PlayerEntity player) {

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

    private void registerInputPackets() {

    }

    private void registerHud() {

        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register(
                (drawContext, tickCounter) -> {

                    if (WorldbreakerClientState.flashIntensity <= 0) return;

                    int alpha = (int)(WorldbreakerClientState.flashIntensity * 255);

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