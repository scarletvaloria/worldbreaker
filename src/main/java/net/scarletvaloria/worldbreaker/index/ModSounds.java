package net.scarletvaloria.worldbreaker.index;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;

public class ModSounds {

public static final SoundEvent TOMAHAWK_DASH = registerSoundEvent("tomahawk_dash");
    public static final SoundEvent WORLDBREAKER_SHOCKWAVE = registerSoundEvent("worldbreaker_shockwave");
    public static final SoundEvent LOCK_ON = registerSoundEvent("railcannon_lock_on");
    public static final SoundEvent FIRE_LOOP = registerSoundEvent("cannon_fire_loop");
    public static final SoundEvent SKYBEAM_HIT = registerSoundEvent("skybeam_hit");
    public static final SoundEvent EAR_RINGING = registerSoundEvent("ear_ringing");
    public static final SoundEvent AMWD_CHARGE_START = registerSoundEvent("amwd_charge_start");
    public static final SoundEvent AMWD_CHARGE_LOOP = registerSoundEvent("amwd_charge_loop");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(WorldbreakerProtocol.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
    public static void registerSounds() {
        WorldbreakerProtocol.LOGGER.info("Registering Mod Sounds for " + WorldbreakerProtocol.MOD_ID);
    }

}
