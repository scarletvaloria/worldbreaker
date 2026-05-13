package net.scarletvaloria.worldbreaker.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.scarletvaloria.worldbreaker.index.ModComponents;
import net.scarletvaloria.worldbreaker.index.ModDataComponents;
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

    public WorldbreakerEngineItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {

        ModComponents.EngineFuelData data =
                stack.getOrDefault(ModDataComponents.FUEL_DATA, new ModComponents.EngineFuelData(Map.of()));

        Map<Item, Integer> current = data.fuelCounts();

        tooltip.add(Text.literal("Required Materials:")
                .formatted(Formatting.GRAY));

        for (var entry : QUOTA.entrySet()) {
            Item item = entry.getKey();
            int required = entry.getValue();
            int count = current.getOrDefault(item, 0);

            Formatting color = (count >= required)
                    ? Formatting.GREEN
                    : Formatting.RED;

            tooltip.add(Text.literal(
                    "- " + item.getName().getString() + ": " + count + "/" + required
            ).formatted(color));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getStackInHand(hand);
        ItemStack offhand = player.getOffHandStack();
        Item item = offhand.getItem();

        if (!QUOTA.containsKey(item)) {
            return TypedActionResult.pass(stack);
        }

        if (world.isClient) {
            return TypedActionResult.success(stack);
        }

        ModComponents.EngineFuelData data =
                stack.getOrDefault(ModDataComponents.FUEL_DATA, new ModComponents.EngineFuelData(Map.of()));

        Map<Item, Integer> map = new HashMap<>(data.fuelCounts());

        int current = map.getOrDefault(item, 0);
        int required = QUOTA.get(item);

        if (current >= required) {
            return TypedActionResult.success(stack);
        }

        offhand.decrement(1);
        map.put(item, current + 1);

        stack.set(ModDataComponents.FUEL_DATA,
                new ModComponents.EngineFuelData(Map.copyOf(map)));

        player.setStackInHand(hand, stack);

        if (player instanceof ServerPlayerEntity sp) {
            sp.currentScreenHandler.sendContentUpdates();
        }

        world.playSound(
                null,
                player.getBlockPos(),
                ModSounds.ENGINE_ASSIMILATE,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );

        player.sendMessage(Text.literal(
                "Assimilated " + item.getName().getString() +
                        ": " + (current + 1) + "/" + required
        ), true);

        if (isFullyFueled(map)) {

            stack.set(ModDataComponents.FUEL_DATA,
                    new ModComponents.EngineFuelData(Map.of()));

            player.setStackInHand(hand, new ItemStack(ModItems.WORLDBREAKER_ASSEMBLY));

            player.sendMessage(
                    Text.literal("Worldbreaker Assembly Complete!")
                            .formatted(Formatting.GOLD, Formatting.BOLD),
                    false
            );

            world.playSound(
                    null,
                    player.getBlockPos(),
                    ModSounds.ENGINE_COMPLETE,
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
            );
        }

        return TypedActionResult.success(stack);
    }

    private static ModComponents.EngineFuelData getOrCreate(ItemStack stack) {
        ModComponents.EngineFuelData data = stack.get(ModDataComponents.FUEL_DATA);

        if (data == null) {
            data = new ModComponents.EngineFuelData(Map.of());
            stack.set(ModDataComponents.FUEL_DATA, data);
        }

        return data;
    }

    private boolean isFullyFueled(Map<Item, Integer> map) {
        return QUOTA.entrySet().stream()
                .allMatch(e -> map.getOrDefault(e.getKey(), 0) >= e.getValue());
    }

}