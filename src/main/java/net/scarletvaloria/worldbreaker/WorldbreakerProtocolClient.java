package net.scarletvaloria.worldbreaker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.scarletvaloria.worldbreaker.index.MarkerPacket;
import net.scarletvaloria.worldbreaker.item.RailcannonItem;
import net.scarletvaloria.worldbreaker.index.ModComponents;

public class WorldbreakerProtocolClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

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
    }
}
