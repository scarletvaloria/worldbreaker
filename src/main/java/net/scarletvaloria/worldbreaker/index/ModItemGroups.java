package net.scarletvaloria.worldbreaker.index;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;
import net.scarletvaloria.worldbreaker.index.ModItems;

public class ModItemGroups {

    public static final ItemGroup ASSORTED_ARSENAL_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(WorldbreakerProtocol.MOD_ID, "worldbreaker_group"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.TOMAHAWK))
                    .displayName(Text.translatable("itemgroup.worldbreaker.worldbreaker_group"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.TOMAHAWK);
                        entries.add(ModItems.WORLDBREAKER_RAILCANNON);
                        entries.add(ModItems.AMWD);
                        entries.add(ModItems.WORLDBREAKER_ENGINE);
                        entries.add(ModItems.PLASMA_CELL);
                        entries.add(ModItems.AURIC_TITANIUM_PLATING);
                        entries.add(ModItems.PLASMA_CATALYST);



                    })


                    .build());

    public static void registerItemGroups() {
        WorldbreakerProtocol.LOGGER.info("Registering Item Groups for " + WorldbreakerProtocol.MOD_ID);
    }
}
