package net.scarletvaloria.worldbreaker.item;

import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.scarletvaloria.worldbreaker.index.ModComponents;
import net.scarletvaloria.worldbreaker.index.ModItems;
import net.scarletvaloria.worldbreaker.index.ModSounds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldbreakerEngineItem extends Item {
    private static final Map<Item, Integer> QUOTA = Map.of(
            Items.IRON_BLOCK, 64,
            Items.GOLD_BLOCK, 64,
            Items.ICE, 64,
            Items.NETHERITE_BLOCK, 8,
            Items.FURNACE, 8,
            Items.NETHER_STAR, 3,
            Items.REPEATER, 16,
            Items.COMPARATOR, 16
    );

    public WorldbreakerEngineItem(Settings settings) { super(settings); }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        ModComponents.EngineFuelData data = stack.get(ModComponents.FUEL_DATA);
        Map<Item, Integer> current = data != null ? data.fuelCounts() : Map.of();

        tooltip.add(Text.literal("Required Materials:").formatted(Formatting.GRAY));
        QUOTA.forEach((item, required) -> {
            int count = current.getOrDefault(item, 0);
            Formatting color = (count >= required) ? Formatting.GREEN : Formatting.RED;
            tooltip.add(Text.literal("- " + item.getName().getString() + ": " + count + "/" + required).formatted(color));
        });
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack engine = player.getStackInHand(hand);
        ItemStack offhand = player.getOffHandStack();
        Item offhandItem = offhand.getItem();

        if (QUOTA.containsKey(offhandItem)) {
            if (!world.isClient) {
                ModComponents.EngineFuelData data = engine.getOrDefault(ModComponents.FUEL_DATA, new ModComponents.EngineFuelData(new HashMap<>()));
                Map<Item, Integer> currentCounts = new HashMap<>(data.fuelCounts());

                int currentCount = currentCounts.getOrDefault(offhandItem, 0);
                int required = QUOTA.get(offhandItem);

                if (currentCount < required) {
                    offhand.decrement(1);
                    currentCounts.put(offhandItem, currentCount + 1);

                    engine.set(ModComponents.FUEL_DATA, new ModComponents.EngineFuelData(currentCounts));

                    world.playSound(null, player.getBlockPos(), ModSounds.ENGINE_ASSIMILATE, SoundCategory.PLAYERS, 1.0f, 0.5f + (currentCount * 0.1f));
                    player.sendMessage(Text.literal("Assimilated " + offhandItem.getName().getString() + ": " + (currentCount + 1) + "/" + required), true);

                    if (isFullyFueled(currentCounts)) {
                        player.setStackInHand(hand, new ItemStack(ModItems.WORLDBREAKER_ASSEMBLY));
                        player.sendMessage(Text.literal("Worldbreaker Assembly Complete!").formatted(Formatting.GOLD, Formatting.BOLD), false);
                        world.playSound(null, player.getBlockPos(), ModSounds.ENGINE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 0.5f + (currentCount * 0.1f));
                    }
                }
            }
            return TypedActionResult.success(engine);
        }
        return TypedActionResult.pass(engine);
    }

    private boolean isFullyFueled(Map<Item, Integer> current) {
        return QUOTA.entrySet().stream().allMatch(e -> current.getOrDefault(e.getKey(), 0) >= e.getValue());
    }
}
