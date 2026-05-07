package net.scarletvaloria.worldbreaker.index;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;

public class ModClientEvents {
    private static float originalVolume = -1f;
    private static PositionedSoundInstance ringingInstance = null;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            var masterVolumeOption = client.options.getSoundVolumeOption(SoundCategory.MASTER);

            if (client.player.hasStatusEffect(ModStatusEffects.CONCUSSED)) {
                if (originalVolume == -1f) {
                    originalVolume = masterVolumeOption.getValue().floatValue();
                }
                masterVolumeOption.setValue(0.1);
            } else {
                if (originalVolume != -1f) {
                    masterVolumeOption.setValue((double) originalVolume);
                    originalVolume = -1f;
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            boolean hasEffect = client.player.hasStatusEffect(ModStatusEffects.CONCUSSED);

            if (hasEffect) {
                if (ringingInstance == null || !client.getSoundManager().isPlaying(ringingInstance)) {
                    ringingInstance = PositionedSoundInstance.master(ModSounds.EAR_RINGING, 1.0f, 1.0f);
                    client.getSoundManager().play(ringingInstance);
                }
            } else {
                if (ringingInstance != null) {
                    client.getSoundManager().stop(ringingInstance);
                    ringingInstance = null;
                }
            }
        });
    }
}
