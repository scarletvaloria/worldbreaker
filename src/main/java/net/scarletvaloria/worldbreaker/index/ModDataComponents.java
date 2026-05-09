package net.scarletvaloria.worldbreaker.index;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final ComponentType<ModComponents.EngineFuelData> FUEL_DATA = register("fuel_data", builder ->
            builder.codec(ModComponents.EngineFuelData.CODEC).packetCodec(ModComponents.EngineFuelData.PACKET_CODEC));

    public static final ComponentType<Integer> DASH_CHARGES = register("dash_charges", builder ->
            builder.codec(Codec.INT));

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of("worldbreaker", id), builderOperator.apply(ComponentType.builder()).build());
    }

    public static void initialize() {}
}
