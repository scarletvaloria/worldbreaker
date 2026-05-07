package net.scarletvaloria.worldbreaker.index;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;
import net.scarletvaloria.worldbreaker.item.RailcannonItem;
import net.scarletvaloria.worldbreaker.item.TomahawkItem;

import static net.acoyt.acornlib.api.util.ItemUtils.modifyItemNameColor;

public class ModItems {
    public static final Item TOMAHAWK = Registry.register(
            Registries.ITEM,
            Identifier.of(WorldbreakerProtocol.MOD_ID, "tomahawk"),
            new TomahawkItem(ToolMaterials.NETHERITE, new Item.Settings().component(ModComponents.DASH_CHARGES, 3))
    );

    public static final Item WORLDBREAKER_RAILCANNON =  Registry.register(
            Registries.ITEM,
            Identifier.of(WorldbreakerProtocol.MOD_ID, "worldbreaker_cannon"),
            new RailcannonItem(new Item.Settings().maxCount(1).fireproof()
    ));

        public static void registerModItems() {
        modifyItemNameColor(TOMAHAWK, 0xffd700);
        modifyItemNameColor(WORLDBREAKER_RAILCANNON, 0xffd700);
        WorldbreakerProtocol.LOGGER.info("Registering Mod Items for" + WorldbreakerProtocol.MOD_ID);
    }
}

