package net.scarletvaloria.worldbreaker.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.scarletvaloria.worldbreaker.index.ModComponents;
import net.scarletvaloria.worldbreaker.index.WorldbreakerFormManager;
import net.scarletvaloria.worldbreaker.index.WorldbreakerState;
import net.scarletvaloria.worldbreaker.index.ModDamageTypes;

import java.util.UUID;

public class WorldbreakerAssemblyItem extends Item {

    private static final UUID OWNER_UUID =
            UUID.fromString("c38f83cf-2723-497a-9327-f5937fb2fc08");

    public WorldbreakerAssemblyItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return TypedActionResult.pass(stack);
        }

        if (world.isClient || hand != Hand.MAIN_HAND) {
            return TypedActionResult.pass(stack);
        }

        if (!serverPlayer.getUuid().equals(OWNER_UUID)) {
            punishUnauthorizedUser(serverPlayer);
            return TypedActionResult.success(stack);
        }

        boolean isCurrentlyActive =
                ModComponents.FORM_STATE.get(serverPlayer).isActive();

        if (isCurrentlyActive) {
            WorldbreakerFormManager.revert(serverPlayer);
        } else {
            WorldbreakerFormManager.transform(serverPlayer);
        }

        ModComponents.FORM_STATE.sync(serverPlayer);
        ModComponents.INVENTORY.sync(serverPlayer);

        return TypedActionResult.success(stack);
    }

    private static void punishUnauthorizedUser(ServerPlayerEntity player) {
        World world = player.getWorld();

        world.createExplosion(
                player,
                player.getX(),
                player.getY(),
                player.getZ(),
                4.0f,
                World.ExplosionSourceType.MOB
        );

        player.setHealth(1.0f);

        player.damage(
                player.getDamageSources().create(
                        ModDamageTypes.ASSEMBLY_REJECTION,
                        player
                ),
                Float.MAX_VALUE
        );
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        if (entity instanceof PlayerEntity player && !world.isClient) {

            var form = ModComponents.FORM_STATE.get(player);

            if (form.getState() != WorldbreakerState.WORLDBREAKER
                    && form.getState() != WorldbreakerState.NORMAL) {
                return;
            }
        }
    }
}