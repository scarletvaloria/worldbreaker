package net.scarletvaloria.worldbreaker;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.entity.effect.StatusEffect;
import net.scarletvaloria.worldbreaker.index.MarkerPacket;
import net.scarletvaloria.worldbreaker.index.ModParticles;
import net.scarletvaloria.worldbreaker.index.ModStatusEffects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.scarletvaloria.worldbreaker.index.ModStatusEffects;
import net.scarletvaloria.worldbreaker.item.RailcannonItem;

public class WorldbreakerProtocolClient implements ClientModInitializer {
    private static float originalVolume = -1f;

    @Override
    public void onInitializeClient() {
        ModParticles.registerParticlesClient();

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient && player.isSneaking() && player.getStackInHand(hand).getItem() instanceof RailcannonItem) {
                HitResult hit = MinecraftClient.getInstance().crosshairTarget;

                if (hit == null || hit.getType() == HitResult.Type.MISS) return TypedActionResult.pass(player.getStackInHand(hand));

                BlockPos pos;
                if (hit.getType() == HitResult.Type.ENTITY) {
                    pos = ((EntityHitResult) hit).getEntity().getBlockPos();
                } else {
                    pos = ((BlockHitResult) hit).getBlockPos();
                }

                ClientPlayNetworking.send(new MarkerPacket(pos));
                return TypedActionResult.success(player.getStackInHand(hand));
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (player.isSneaking() && player.getStackInHand(hand).getItem() instanceof RailcannonItem) {
                if (world.isClient) {
                    HitResult hit = MinecraftClient.getInstance().crosshairTarget;
                    if (hit != null && hit.getType() != HitResult.Type.MISS) {
                        BlockPos pos = (hit.getType() == HitResult.Type.ENTITY) ?
                                ((EntityHitResult) hit).getEntity().getBlockPos() :
                                ((BlockHitResult) hit).getBlockPos();

                        ClientPlayNetworking.send(new MarkerPacket(pos));
                        return TypedActionResult.success(player.getStackInHand(hand));
                    }
                }
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            var masterVolume = client.options.getSoundVolumeOption(SoundCategory.MASTER);

            if (client.player.hasStatusEffect(ModStatusEffects.CONCUSSED)) {
                if (originalVolume == -1f) {
                    originalVolume = masterVolume.getValue().floatValue();
                }
                masterVolume.setValue(0.1);
            } else if (originalVolume != -1f) {
                masterVolume.setValue((double) originalVolume);
                originalVolume = -1f;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            var masterVolume = client.options.getSoundVolumeOption(SoundCategory.MASTER);

            if (client.player.hasStatusEffect(ModStatusEffects.CONCUSSED)) {
                if (originalVolume == -1f) {
                    originalVolume = masterVolume.getValue().floatValue();
                }
                masterVolume.setValue(0.1);
            } else if (originalVolume != -1f) {
                masterVolume.setValue((double) originalVolume);
                originalVolume = -1f;
            }
        });
    }
}
