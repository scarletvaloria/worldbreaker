package net.scarletvaloria.worldbreaker.index;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;

import java.util.function.UnaryOperator;

public abstract class ModComponents implements Component {

    public static final ComponentKey<Component> AMWD_TICKS =
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of("worldbreaker", "amwd_ticks"), Component.class);

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

    public abstract int getTicks();

    public abstract void setTicks(int ticks);

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {

    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {

    }
}
