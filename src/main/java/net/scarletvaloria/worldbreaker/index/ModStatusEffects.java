package net.scarletvaloria.worldbreaker.index;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;

public class ModStatusEffects {
    public static RegistryEntry<StatusEffect> CONCUSSED;

    public static void registerEffects() {
        CONCUSSED = Registry.registerReference(
                Registries.STATUS_EFFECT,
                Identifier.of("worldbreaker", "concussed"),
                new ConcussedEffect()
        );
    }
}