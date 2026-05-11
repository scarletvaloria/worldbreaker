package net.scarletvaloria.worldbreaker;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import software.bernie.geckolib.constant.dataticket.SerializableDataTicket;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.acoyt.acornlib.api.event.CustomRiptideEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import net.scarletvaloria.worldbreaker.index.*;
import net.scarletvaloria.worldbreaker.item.RailcannonItem;
import net.scarletvaloria.worldbreaker.item.TomahawkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static net.scarletvaloria.worldbreaker.item.TomahawkItem.triggerShockwave;

public class WorldbreakerProtocol implements ModInitializer {
    public static final String MOD_ID = "worldbreaker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Set<UUID> DIVING_PLAYERS = new HashSet<>();
    public static final Set<UUID> AIRBORNE_DIVING_PLAYERS = new HashSet<>();
    public static final Set<UUID> DOWNED_PLAYERS = new HashSet<>();

    public static final SerializableDataTicket<Double> CHARGE_LEVEL =
            GeckoLibUtil.addDataTicket(SerializableDataTicket.ofDouble(id("charge_level")));


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

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {

            UUID uuid = newPlayer.getUuid();

            if (WorldbreakerProtocol.DOWNED_PLAYERS.contains(uuid)) {

                WorldbreakerProtocol.DOWNED_PLAYERS.remove(uuid);

                WorldbreakerFormManager.transform(newPlayer);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

                UUID uuid = player.getUuid();

                if (DIVING_PLAYERS.contains(uuid)) {

                    if (!player.isOnGround()) {
                        AIRBORNE_DIVING_PLAYERS.add(uuid);
                    }

                    if (player.isOnGround() && AIRBORNE_DIVING_PLAYERS.contains(uuid)) {

                        float fallDist = player.fallDistance;

                        if (fallDist > 8.0f) {
                            triggerShockwave(player, fallDist);
                        }

                        player.fallDistance = 0;

                        DIVING_PLAYERS.remove(uuid);
                        AIRBORNE_DIVING_PLAYERS.remove(uuid);
                    }
                }
            }
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {

            if (!(entity instanceof ServerPlayerEntity player))
                return true;

            var form = ModComponents.FORM_STATE.get(player);

            if (source.isOf(DamageTypes.FALL)
                    && DIVING_PLAYERS.contains(player.getUuid())) {
                return false;
            }

            float newHealth = player.getHealth() - amount;

            if (newHealth > 0.0f) {
                return true;
            }

            if (form.getState() == WorldbreakerState.WORLDBREAKER) {

                player.setHealth(1.0f);

                player.getServer().execute(() -> {

                    WorldbreakerDeathHandler.playDeathShockwave(player);

                    WorldbreakerFormManager.revert(player);

                    player.damage(
                            player.getDamageSources().create(ModDamageTypes.WORLDBREAKER_DEATH),
                            Float.MAX_VALUE
                    );
                });

                return false;
            }

            return true;
        });

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {

            if (!(entity instanceof ServerPlayerEntity player))
                return true;

            var form = ModComponents.FORM_STATE.get(player);

            if (form.getState() == WorldbreakerState.WORLDBREAKER) {

                WorldbreakerDeathHandler.playDeathShockwave(player);

                form.setState(WorldbreakerState.TRUE_DEATH);
                return true;
            }

            return true;
        });

        CustomRiptideEvent.EVENT.register((player, stack) -> {
            if (stack.getItem() instanceof TomahawkItem) {
                return Optional.of(id("textures/entity/tomahawk_riptide.png"));
            }
            return Optional.empty();
        });

        PayloadTypeRegistry.playC2S().register(MarkerPacket.ID, MarkerPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MarkerPacket.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                ItemStack stack = player.getMainHandStack();
                if (stack.getItem() instanceof RailcannonItem railItem) {
                    if (stack.contains(ModComponents.MARKER_POS)) {
                        railItem.fireSkyBeam(player.getServerWorld(), player, stack.get(ModComponents.MARKER_POS), stack);
                        stack.remove(ModComponents.MARKER_POS);
                    } else {
                        stack.set(ModComponents.MARKER_POS, payload.pos());
                        player.sendMessage(Text.literal("§e§lTARGET ACQUIRED: §f" + payload.pos().toShortString()), true);
                        player.getServerWorld().playSound(null, payload.pos(), ModSounds.LOCK_ON, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    }
                }
            });
        });
    }
}