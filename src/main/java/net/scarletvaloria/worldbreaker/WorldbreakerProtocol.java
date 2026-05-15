package net.scarletvaloria.worldbreaker;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.scarletvaloria.worldbreaker.index.ModDamageTypes;

import net.scarletvaloria.worldbreaker.index.*;
import net.scarletvaloria.worldbreaker.item.RailcannonItem;
import net.scarletvaloria.worldbreaker.item.TomahawkItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WorldbreakerProtocol implements ModInitializer {

    public static final String MOD_ID = "worldbreaker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Set<UUID> DIVING_PLAYERS = new HashSet<>();
    public static final Set<UUID> AIRBORNE_DIVING_PLAYERS = new HashSet<>();
    public static final Set<UUID> DASH_STATE = new HashSet<>();
    public static final Map<UUID, Integer> DASH_TIMER = new HashMap<>();
    public static final Set<UUID> GRAVITY_DOMAIN_ACTIVE = new HashSet<>();
    public static final Set<UUID> DASH_FLIGHT_LOCK = new HashSet<>();
    public static final Set<UUID> DASH_IMMUNITY = new HashSet<>();
    public static final Map<UUID, Integer> GRAVITY_DOMAIN_TIMER = new HashMap<>();
    public static final Map<UUID, Integer> WORLDBREAKER_DEATH_SEQUENCE = new HashMap<>();
    public static final Set<UUID> WORLDBREAKER_DEATH_FINISHING = new HashSet<>();

    public static final Set<UUID> DIED_IN_WORLDBREAKER = new HashSet<>();

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {

        ModSounds.registerSounds();
        ModComponents.initialize();
        ModDataComponents.initialize();
        ModStatusEffects.registerEffects();
        ModParticles.registerParticles();
        ModItems.registerModItems();
        ModItemGroups.registerItemGroups();

        registerNetworking();
        registerEvents();
    }

    private void registerNetworking() {

        PayloadTypeRegistry.playC2S().register(DashPacket.ID, DashPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(DiveTogglePacket.ID, DiveTogglePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(MarkerPacket.ID, MarkerPacket.CODEC);

        PayloadTypeRegistry.playS2C().register(
                WorldbreakerFlashPacket.ID,
                WorldbreakerFlashPacket.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(DashPacket.ID,
                (payload, ctx) -> ctx.server().execute(() ->
                        WorldbreakerFlightController.tryDash(ctx.player())
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(DiveTogglePacket.ID,
                (payload, ctx) -> ctx.server().execute(() ->
                        WorldbreakerDiveSystem.toggle(ctx.player())
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(MarkerPacket.ID,
                (payload, ctx) -> ctx.server().execute(() -> {

                    ServerPlayerEntity player = ctx.player();
                    ItemStack stack = player.getMainHandStack();

                    if (!(stack.getItem() instanceof RailcannonItem railItem)) return;

                    if (stack.contains(ModComponents.MARKER_POS)) {

                        railItem.fireSkyBeam(
                                player.getServerWorld(),
                                player,
                                stack.get(ModComponents.MARKER_POS),
                                stack
                        );

                        stack.remove(ModComponents.MARKER_POS);

                    } else {

                        stack.set(ModComponents.MARKER_POS, payload.pos());

                        player.getServerWorld().playSound(
                                null,
                                payload.pos(),
                                ModSounds.LOCK_ON,
                                SoundCategory.PLAYERS,
                                1.0f,
                                1.0f
                        );
                    }
                })
        );
    }

    private void registerEvents() {

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {

            if (!(entity instanceof ServerPlayerEntity player)) {
                return true;
            }

            UUID id = player.getUuid();

            if (WORLDBREAKER_DEATH_FINISHING.remove(id)) {
                DIED_IN_WORLDBREAKER.add(id);
                WORLDBREAKER_DEATH_SEQUENCE.remove(id);
                return true;
            }

            if (ModComponents.FORM_STATE.get(player).getState()
                    != WorldbreakerState.WORLDBREAKER) {
                return true;
            }

            startWorldbreakerDeathSequence(player);

            return false;
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {

            UUID id = newPlayer.getUuid();

            if (!DIED_IN_WORLDBREAKER.remove(id)) {
                return;
            }

            newPlayer.getServer().execute(() -> {

                newPlayer.clearStatusEffects();

                newPlayer.getAttributeInstance(EntityAttributes.GENERIC_SCALE)
                        .setBaseValue(1.0);

                newPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                        .setBaseValue(20.0);

                newPlayer.getAttributeInstance(EntityAttributes.GENERIC_ARMOR)
                        .setBaseValue(0.0);

                newPlayer.setHealth(20.0f);

                newPlayer.getAbilities().allowFlying = false;
                newPlayer.getAbilities().flying = false;
                newPlayer.sendAbilitiesUpdate();

                var form = ModComponents.FORM_STATE.get(newPlayer);
                form.setState(WorldbreakerState.NORMAL);
                ModComponents.FORM_STATE.sync(newPlayer);

                removeWorldbreakerItems(newPlayer);
                giveAssembly(newPlayer);

                newPlayer.getServerWorld().playSound(
                        null,
                        newPlayer.getX(),
                        newPlayer.getY(),
                        newPlayer.getZ(),
                        SoundEvents.BLOCK_BEACON_DEACTIVATE,
                        SoundCategory.PLAYERS,
                        1.5f,
                        1.0f
                );
            });
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {

            if (entity instanceof ServerPlayerEntity player) {

                UUID id = player.getUuid();

                if (WORLDBREAKER_DEATH_FINISHING.contains(id)) {
                    return true;
                }

                if (WORLDBREAKER_DEATH_SEQUENCE.containsKey(id)) {
                    return false;
                }

                if (!WorldbreakerCombatHandler.onDamage(player, source, amount)) {
                    return false;
                }
            }

            if (entity instanceof ServerPlayerEntity player
                    && source.isOf(DamageTypes.FALL)
                    && DIVING_PLAYERS.contains(player.getUuid())) {
                return false;
            }

            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

                if (WORLDBREAKER_DEATH_SEQUENCE.containsKey(player.getUuid())) {
                    tickWorldbreakerDeathSequence(player);
                    continue;
                }

                UUID id = player.getUuid();

                if (DIVING_PLAYERS.contains(id)) {

                    if (!player.isOnGround()) {
                        AIRBORNE_DIVING_PLAYERS.add(id);
                    } else if (AIRBORNE_DIVING_PLAYERS.contains(id)) {

                        float fallDistance = player.fallDistance;

                        TomahawkItem.triggerShockwave(player, Math.max(fallDistance, 8.0f));

                        player.fallDistance = 0.0f;

                        DIVING_PLAYERS.remove(id);
                        AIRBORNE_DIVING_PLAYERS.remove(id);
                    }
                }

                WorldbreakerAbilitySystem.tick(player);
                WorldbreakerDiveSystem.tick(player);
                WorldbreakerFlightController.tick(player);
                WorldbreakerCombatHandler.tick(player);
            }
        });
    }

    private static void startWorldbreakerDeathSequence(ServerPlayerEntity player) {

        UUID id = player.getUuid();

        if (WORLDBREAKER_DEATH_SEQUENCE.containsKey(id)) {
            return;
        }

        player.setHealth(1.0f);
        player.extinguish();

        WORLDBREAKER_DEATH_SEQUENCE.put(id, 200);

        WorldbreakerFormManager.flashNearbyPlayers(player);

        knockBackNearbyPlayers(player, 14.0, 2.2, 0.9);

        player.getServerWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.WORLDBREAKER_SHOCKWAVE,
                SoundCategory.PLAYERS,
                1.5f,
                0.0f
        );
    }

    private static void tickWorldbreakerDeathSequence(ServerPlayerEntity player) {

        UUID id = player.getUuid();

        int ticksLeft = WORLDBREAKER_DEATH_SEQUENCE.getOrDefault(id, 0);

        if (ticksLeft <= 0) {
            finishWorldbreakerDeathSequence(player);
            return;
        }

        WORLDBREAKER_DEATH_SEQUENCE.put(id, ticksLeft - 1);

        ServerWorld world = player.getServerWorld();

        player.setVelocity(Vec3d.ZERO);
        player.velocityModified = true;
        player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));

        double progress = 1.0 - (ticksLeft / 200.0);

        world.spawnParticles(
                ParticleTypes.LARGE_SMOKE,
                player.getX(),
                player.getY() + 1.0,
                player.getZ(),
                2 + (int) (progress * 10),
                0.25 + progress * 0.5,
                0.4,
                0.25 + progress * 0.5,
                0.02
        );

        world.spawnParticles(
                ModParticles.SHOCKWAVE_FLAME,
                player.getX(),
                player.getY() + 1.0,
                player.getZ(),
                1 + (int) (progress * 6),
                0.35,
                0.6,
                0.35,
                0.015
        );

        if (ticksLeft == 160 || ticksLeft == 100 || ticksLeft == 40 || ticksLeft == 10) {
            WorldbreakerFormManager.flashNearbyPlayers(player);
        }
    }

    private static void finishWorldbreakerDeathSequence(ServerPlayerEntity player) {

        UUID id = player.getUuid();

        WORLDBREAKER_DEATH_SEQUENCE.remove(id);
        WORLDBREAKER_DEATH_FINISHING.add(id);
        DIED_IN_WORLDBREAKER.add(id);

        ServerWorld world = player.getServerWorld();

        WorldbreakerFormManager.flashNearbyPlayers(player);

        knockBackNearbyPlayers(player, 18.0, 3.5, 1.3);

        world.spawnParticles(
                ModParticles.EXPANDING_RING,
                player.getX(),
                player.getY() + 0.05,
                player.getZ(),
                1,
                0,
                0,
                0,
                0
        );

        world.spawnParticles(
                ParticleTypes.LARGE_SMOKE,
                player.getX(),
                player.getY() + 1.0,
                player.getZ(),
                80,
                1.2,
                0.9,
                1.2,
                0.08
        );

        world.spawnParticles(
                ParticleTypes.END_ROD,
                player.getX(),
                player.getY() + 1.0,
                player.getZ(),
                60,
                1.0,
                1.0,
                1.0,
                0.05
        );

        world.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.WORLDBREAKER_SHOCKWAVE,
                SoundCategory.PLAYERS,
                2.0f,
                0.8f
        );

        player.setHealth(1.0f);

        player.damage(
                world.getDamageSources().create(
                        ModDamageTypes.WORLDBREAKER_DEATH,
                        player
                ),
                Float.MAX_VALUE
        );
    }

    private static void knockBackNearbyPlayers(
            ServerPlayerEntity center,
            double radius,
            double horizontalStrength,
            double verticalStrength
    ) {

        ServerWorld world = center.getServerWorld();

        Box box = center.getBoundingBox().expand(radius);

        for (ServerPlayerEntity other : world.getEntitiesByClass(
                ServerPlayerEntity.class,
                box,
                other -> other != center && other.squaredDistanceTo(center) <= radius * radius
        )) {

            Vec3d diff = other.getPos().subtract(center.getPos());

            Vec3d direction = new Vec3d(diff.x, 0.0, diff.z);

            if (direction.lengthSquared() < 0.001) {
                direction = new Vec3d(
                        center.getRandom().nextDouble() - 0.5,
                        0.0,
                        center.getRandom().nextDouble() - 0.5
                );
            }

            direction = direction.normalize();

            other.addVelocity(
                    direction.x * horizontalStrength,
                    verticalStrength,
                    direction.z * horizontalStrength
            );

            other.velocityModified = true;
            other.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(other));
        }
    }

    private static void removeAssembly(ServerPlayerEntity player) {

        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);

            if (stack.isOf(ModItems.WORLDBREAKER_ASSEMBLY)) {
                inv.setStack(i, ItemStack.EMPTY);
            }
        }
    }

    private static void removeWorldbreakerItems(ServerPlayerEntity player) {

        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < inv.size(); i++) {

            ItemStack stack = inv.getStack(i);

            if (stack.isOf(ModItems.TOMAHAWK)
                    || stack.isOf(ModItems.WORLDBREAKER_RAILCANNON)
                    || stack.isOf(ModItems.AMWD)
                    || stack.isOf(ModItems.PLASMA_CELL)
                    || stack.isOf(ModItems.WORLDBREAKER_ASSEMBLY)) {

                inv.setStack(i, ItemStack.EMPTY);
            }
        }
    }

    private static void giveAssembly(ServerPlayerEntity player) {

        removeAssembly(player);

        ItemStack assembly = new ItemStack(ModItems.WORLDBREAKER_ASSEMBLY);
        assembly.set(ModDataComponents.WORLDBREAKER_ITEM, true);

        player.getInventory().insertStack(assembly);
    }
}