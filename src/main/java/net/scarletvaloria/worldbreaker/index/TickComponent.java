package net.scarletvaloria.worldbreaker.index;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class TickComponent implements Component, ServerTickingComponent {
    private int ticks = 0;
    private final LivingEntity owner;

    public TickComponent(LivingEntity owner) {
        this.owner = owner;
    }


    public int getTicks() { return ticks; }
    public void setTicks(int ticks) { this.ticks = ticks; }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.ticks = tag.getInt("ticks");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("ticks", this.ticks);
    }

    @Override
    public void serverTick() {

    }
}
