package net.scarletvaloria.worldbreaker.index;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;

public interface ModParticles {

    SimpleParticleType SHOCKWAVE_FLAME = FabricParticleTypes.simple(true);
    SimpleParticleType EXPANDING_RING = FabricParticleTypes.simple(true);

    private static void create(String name, SimpleParticleType particle) {
        Registry.register(
                Registries.PARTICLE_TYPE,
                Identifier.of(WorldbreakerProtocol.MOD_ID, name),
                particle
        );
    }

    static void registerParticles() {
        create("shockwave_flame", SHOCKWAVE_FLAME);
        create("expanding_ring", EXPANDING_RING);
    }

    static void registerParticlesClient() {
        ParticleFactoryRegistry.getInstance().register(
                SHOCKWAVE_FLAME,
                FlameParticle.Factory::new
        );

        ParticleFactoryRegistry.getInstance().register(
                EXPANDING_RING,
                ExpandingRingParticle.Factory::new
        );
    }
}