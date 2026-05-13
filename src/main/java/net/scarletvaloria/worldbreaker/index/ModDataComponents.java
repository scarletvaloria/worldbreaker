package net.scarletvaloria.worldbreaker.index;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;

public class ModDataComponents {

    public static final ComponentType<ModComponents.EngineFuelData> FUEL_DATA =
            Registry.register(Registries.DATA_COMPONENT_TYPE,
                    Identifier.of(WorldbreakerProtocol.MOD_ID, "fuel_data"),
                    ComponentType.<ModComponents.EngineFuelData>builder()
                            .codec(ModComponents.EngineFuelData.CODEC)
                            .packetCodec(ModComponents.EngineFuelData.PACKET_CODEC)
                            .build()
            );

    public static final ComponentType<Integer> DASH_CHARGES =
            Registry.register(Registries.DATA_COMPONENT_TYPE,
                    Identifier.of(WorldbreakerProtocol.MOD_ID, "dash_charges"),
                    ComponentType.<Integer>builder().codec(Codec.INT).build()
            );

    public static final ComponentType<Boolean> WORLDBREAKER_ITEM =
            Registry.register(Registries.DATA_COMPONENT_TYPE,
                    Identifier.of(WorldbreakerProtocol.MOD_ID, "worldbreaker_item"),
                    ComponentType.<Boolean>builder().codec(Codec.BOOL).build()
            );

    public static void initialize() {}
}