package net.scarletvaloria.worldbreaker.index;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;

public interface ModDamageTypes {
    RegistryKey<DamageType> SHOCKWAVE_KILL = of("shockwave_damage");
    RegistryKey<DamageType> RAILCANNON_KILL = of("laser_damage");
    RegistryKey<DamageType> SKYBEAM_KILL = of("skybeam_damage");
    RegistryKey<DamageType> LAUNCH_KILL = of("launch_damage");
    RegistryKey<DamageType> WORLDBREAKER_DEATH = of("worldbreaker_death_damage");


    static DamageSource shockwave_kill(LivingEntity entity) {
        return entity.getDamageSources().create(SHOCKWAVE_KILL); }
    static DamageSource railcannon_kill(LivingEntity entity) {
        return entity.getDamageSources().create(RAILCANNON_KILL); }
    static DamageSource skybeam_kill(LivingEntity entity) {
        return entity.getDamageSources().create(SKYBEAM_KILL); }
    static DamageSource launch_kill(LivingEntity entity) {
        return entity.getDamageSources().create(LAUNCH_KILL); }
    static DamageSource worldbreaker_death(LivingEntity entity) {
        return entity.getDamageSources().create(WORLDBREAKER_DEATH); }

    private static RegistryKey<DamageType> of(String name) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, WorldbreakerProtocol.id(name));
    }
}

