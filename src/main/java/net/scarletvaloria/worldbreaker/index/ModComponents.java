package net.scarletvaloria.worldbreaker.index;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
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
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class ModComponents implements EntityComponentInitializer {
    public static void initialize() {}
    public static final ComponentKey<Component> AMWD_TICKS =
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of(WorldbreakerProtocol.MOD_ID, "amwd_ticks"), Component.class);

    public static final ComponentType<BlockPos> MARKER_POS = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(WorldbreakerProtocol.MOD_ID, "marker_pos"),
            ComponentType.<BlockPos>builder().codec(BlockPos.CODEC).build()
    );

    public static final ComponentType<EngineFuelData> FUEL_DATA = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("worldbreaker", "fuel_data"),
            ComponentType.<EngineFuelData>builder()
                    .codec(EngineFuelData.CODEC)
                    .packetCodec(EngineFuelData.PACKET_CODEC)
                    .build()
    );

    public record EngineFuelData(Map<Item, Integer> fuelCounts) {
        public static final Codec<EngineFuelData> CODEC = Codec.unboundedMap(
                Registries.ITEM.getCodec(), Codec.INT).xmap(EngineFuelData::new, EngineFuelData::fuelCounts);

        public static final PacketCodec<RegistryByteBuf, EngineFuelData> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.map(HashMap::new, PacketCodecs.registryValue(RegistryKeys.ITEM), PacketCodecs.VAR_INT),
                EngineFuelData::fuelCounts,
                EngineFuelData::new
        );
    }

    public static final ComponentKey<FormStateComponent> FORM_STATE =
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of("worldbreaker", "form_state"), FormStateComponent.class);

    public static final ComponentKey<InventoryComponent> INVENTORY =
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of(WorldbreakerProtocol.MOD_ID, "inventory"), InventoryComponent.class);

    public class InventoryComponent implements Component {
        private final DefaultedList<ItemStack> savedInventory = DefaultedList.ofSize(41, ItemStack.EMPTY);
        private final PlayerEntity player;

        public InventoryComponent(PlayerEntity player) { this.player = player; }

        public void save(PlayerInventory inventory) {
            for (int i = 0; i < inventory.size(); i++) {
                this.savedInventory.set(i, inventory.getStack(i).copy());
            }
        }

        public void restore(PlayerInventory inventory) {
            inventory.clear();
            for (int i = 0; i < this.savedInventory.size(); i++) {
                inventory.setStack(i, this.savedInventory.get(i).copy());
            }
        }

        @Override
        public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            Inventories.readNbt(nbt, savedInventory, lookup);
        }

        @Override
        public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            Inventories.writeNbt(nbt, savedInventory, lookup);
        }
    }


    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(FORM_STATE, FormStateComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(INVENTORY, InventoryComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                Identifier.of("worldbreaker", id),
                (builderOperator.apply(ComponentType.builder())).build()

        );
    }
}

