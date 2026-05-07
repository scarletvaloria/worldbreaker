package net.scarletvaloria.worldbreaker.index;

import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.function.UnaryOperator;

public class ModComponents {

    public static final ComponentType<Integer> DASH_CHARGES = register("dash_charges", builder ->
            builder.codec(Codec.INT)
    );

    public static final ComponentType<BlockPos> MARKER_POS = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(WorldbreakerProtocol.MOD_ID, "marker_pos"),
            ComponentType.<BlockPos>builder().codec(BlockPos.CODEC).build()
    );

    public static void initialize() {
    }

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                Identifier.of(WorldbreakerProtocol.MOD_ID, id),
                (builderOperator.apply(ComponentType.builder())).build()

        );
    }
}
