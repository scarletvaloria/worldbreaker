package net.scarletvaloria.worldbreaker.index;

import net.minecraft.registry.Registries;
import net.scarletvaloria.worldbreaker.index.ModStatusEffects;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class ConcussedRingingSound extends MovingSoundInstance {
    private final ClientPlayerEntity player;

    public ConcussedRingingSound(ClientPlayerEntity player, SoundEvent event) {
        super(event, SoundCategory.PLAYERS, player.getRandom());
        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.6f;
    }

    @Override
    public void tick() {
        if (!player.isRemoved() && player.hasStatusEffect(ModStatusEffects.CONCUSSED)) {
            this.x = (float) player.getX();
            this.y = (float) player.getY();
            this.z = (float) player.getZ();
        } else {
            this.setDone();
        }
    }
}