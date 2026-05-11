package net.scarletvaloria.worldbreaker;

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



        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

                UUID uuid = player.getUuid();

                if (!DIVING_PLAYERS.contains(uuid))
                    continue;

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
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player
                    && source.isOf(DamageTypes.FALL)
                    && DIVING_PLAYERS.contains(player.getUuid())) {

                return false;
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