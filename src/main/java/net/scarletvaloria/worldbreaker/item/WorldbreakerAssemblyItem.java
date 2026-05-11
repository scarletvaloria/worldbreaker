package net.scarletvaloria.worldbreaker.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.scarletvaloria.worldbreaker.index.ModComponents;
import net.scarletvaloria.worldbreaker.index.WorldbreakerFormManager;

import java.util.UUID;

public class WorldbreakerAssemblyItem extends Item {



    public WorldbreakerAssemblyItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return TypedActionResult.pass(stack);
        }

        if (!world.isClient && hand == Hand.MAIN_HAND) {

            boolean isCurrentlyActive = ModComponents.FORM_STATE.get(player).isActive();

            if (isCurrentlyActive) {
                WorldbreakerFormManager.revert(serverPlayer);
            } else {
                WorldbreakerFormManager.transform(serverPlayer);
            }

            ModComponents.FORM_STATE.sync(serverPlayer);
            ModComponents.INVENTORY.sync(serverPlayer);

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }
}

