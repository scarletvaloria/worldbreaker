package net.scarletvaloria.worldbreaker;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.scarletvaloria.worldbreaker.index.*;
import net.scarletvaloria.worldbreaker.item.RailcannonItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static net.scarletvaloria.worldbreaker.item.TomahawkItem.triggerShockwave;

public class WorldbreakerProtocol implements ModInitializer {

    public static final String MOD_ID = "worldbreaker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Set<UUID> DIVING_PLAYERS = new HashSet<>();
    public static final Set<UUID> AIRBORNE_DIVING_PLAYERS = new HashSet<>();

    public static final Set<UUID> DASH_STATE = new HashSet<>();
    public static final Map<UUID, Integer> DASH_TIMER = new HashMap<>();
    public static final Set<UUID> DASH_FLIGHT_LOCK = new HashSet<>();
    public static final Set<UUID> DASH_IMMUNITY = new HashSet<>();

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

        PayloadTypeRegistry.playC2S().register(DashPacket.ID, DashPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(DiveTogglePacket.ID, DiveTogglePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(MarkerPacket.ID, MarkerPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DashPacket.ID, (payload, ctx) ->
                ctx.server().execute(() ->
                        WorldbreakerFlightController.tryDash(ctx.player())
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(DiveTogglePacket.ID, (payload, ctx) ->
                ctx.server().execute(() ->
                        WorldbreakerDiveSystem.toggle(ctx.player())
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(MarkerPacket.ID, (payload, ctx) -> {
            ctx.server().execute(() -> {

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

                    player.sendMessage(
                            Text.literal("§e§lTARGET ACQUIRED: §f" + payload.pos().toShortString()),
                            true
                    );

                    player.getServerWorld().playSound(
                            null,
                            payload.pos(),
                            ModSounds.LOCK_ON,
                            SoundCategory.PLAYERS,
                            1.0f,
                            1.0f
                    );
                }
            });
        });

        ServerTickEvents.END_SERVER_TICK.register(WorldbreakerProtocol::serverTick);

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {

            if (entity instanceof ServerPlayerEntity player
                    && source.isOf(DamageTypes.FALL)
                    && DIVING_PLAYERS.contains(player.getUuid())) {
                return false;
            }

            return true;
        });

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {

            if (!(entity instanceof ServerPlayerEntity player)) {
                return true;
            }

            var form = ModComponents.FORM_STATE.get(player);

            if (form.getState() != WorldbreakerState.WORLDBREAKER) {
                return true;
            }

            var inv = ModComponents.INVENTORY.get(player);

            player.getInventory().clear();
            inv.restore(player.getInventory());
            WorldbreakerFormManager.revert(player);

            return true;
        });
    }

    private static void serverTick(MinecraftServer server) {

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            UUID id = player.getUuid();

            if (DIVING_PLAYERS.contains(id)) {

                if (!player.isOnGround()) {
                    AIRBORNE_DIVING_PLAYERS.add(id);
                    continue;
                }

                if (AIRBORNE_DIVING_PLAYERS.contains(id)) {

                    float fallDist = player.fallDistance;

                    if (fallDist > 8.0f) {
                        triggerShockwave(player, fallDist);
                    }

                    player.fallDistance = 0;
                    DIVING_PLAYERS.remove(id);
                    AIRBORNE_DIVING_PLAYERS.remove(id);
                }
            }

            WorldbreakerAbilitySystem.tick(player);
            WorldbreakerDiveSystem.tick(player);
            WorldbreakerFlightController.tick(player);
        }
    }
}