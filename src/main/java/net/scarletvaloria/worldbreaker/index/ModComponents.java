package net.scarletvaloria.worldbreaker.index;

import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import com.mojang.serialization.Codec;

import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;

import java.util.HashMap;
import java.util.Map;

public class ModComponents implements EntityComponentInitializer {

    public static void initialize() {}


    public static final ComponentKey<FormStateComponent> FORM_STATE =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                    Identifier.of(WorldbreakerProtocol.MOD_ID, "form_state"),
                    FormStateComponent.class
            );


    public static final ComponentKey<InventoryComponent> INVENTORY =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                    Identifier.of(WorldbreakerProtocol.MOD_ID, "inventory"),
                    InventoryComponent.class
            );


    public static final ComponentType<BlockPos> MARKER_POS = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(WorldbreakerProtocol.MOD_ID, "marker_pos"),
            ComponentType.<BlockPos>builder().codec(BlockPos.CODEC).build()
    );

    public static final ComponentKey<Component> AMWD_TICKS =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                    Identifier.of(WorldbreakerProtocol.MOD_ID, "amwd_ticks"),
                    Component.class
            );


    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {

        registry.registerForPlayers(
                FORM_STATE,
                FormStateComponent::new,
                RespawnCopyStrategy.ALWAYS_COPY
        );

        registry.registerForPlayers(
                INVENTORY,
                (PlayerEntity player) -> new InventoryComponent(),
                RespawnCopyStrategy.NEVER_COPY
        );
    }

    public static class InventoryComponent implements Component {

        private final DefaultedList<ItemStack> savedInventory =
                DefaultedList.ofSize(41, ItemStack.EMPTY);

        public InventoryComponent() {}

        public void save(PlayerInventory inventory) {
            for (int i = 0; i < inventory.size(); i++) {
                savedInventory.set(i, inventory.getStack(i).copy());
            }
        }

        public void dropSavedInventory(ServerPlayerEntity player) {

            for (int i = 0; i < savedInventory.size(); i++) {

                ItemStack stack = savedInventory.get(i);

                if (stack.isEmpty()) {
                    continue;
                }

                if (stack.isOf(ModItems.TOMAHAWK)
                        || stack.isOf(ModItems.WORLDBREAKER_RAILCANNON)
                        || stack.isOf(ModItems.AMWD)
                        || stack.isOf(ModItems.PLASMA_CELL)
                        || stack.isOf(ModItems.WORLDBREAKER_ASSEMBLY)) {
                    continue;
                }

                player.dropItem(stack.copy(), true, false);
                savedInventory.set(i, ItemStack.EMPTY);
            }
        }

        public void restore(PlayerInventory inventory) {
            inventory.clear();

            for (int i = 0; i < savedInventory.size(); i++) {
                ItemStack stack = savedInventory.get(i);
                if (!stack.isEmpty()) {
                    inventory.setStack(i, stack.copy());
                }
            }

            inventory.markDirty();
        }

        @Override
        public void readFromNbt(net.minecraft.nbt.NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            Inventories.readNbt(nbt, savedInventory, lookup);
        }

        @Override
        public void writeToNbt(net.minecraft.nbt.NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            Inventories.writeNbt(nbt, savedInventory, lookup);
        }
    }

    public record EngineFuelData(Map<Item, Integer> fuelCounts) {

        public static final Codec<EngineFuelData> CODEC =
                Codec.unboundedMap(
                        Registries.ITEM.getCodec(),
                        Codec.INT
                ).xmap(EngineFuelData::new, EngineFuelData::fuelCounts);

        public static final PacketCodec<RegistryByteBuf, EngineFuelData> PACKET_CODEC =
                PacketCodec.tuple(
                        PacketCodecs.map(
                                HashMap::new,
                                PacketCodecs.registryValue(RegistryKeys.ITEM),
                                PacketCodecs.VAR_INT
                        ),
                        EngineFuelData::fuelCounts,
                        EngineFuelData::new
                );
    }
}