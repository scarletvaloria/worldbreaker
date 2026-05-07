package net.scarletvaloria.worldbreaker;

import net.acoyt.acornlib.api.event.CustomRiptideEvent;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.scarletvaloria.worldbreaker.index.*;
import net.scarletvaloria.worldbreaker.item.RailcannonItem;
import net.scarletvaloria.worldbreaker.item.TomahawkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import static net.scarletvaloria.worldbreaker.item.TomahawkItem.triggerShockwave;
import net.minecraft.text.Text;


public class WorldbreakerProtocol implements ModInitializer {
    public static final String MOD_ID = "worldbreaker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(WorldbreakerProtocol.MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModSounds.registerSounds();
        ModStatusEffects.registerEffects();
        ModParticles.registerParticles();
        ModItems.registerModItems();
        ModItemGroups.registerItemGroups();


        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player && source.isOf(DamageTypes.FALL)) {
                if (player.getCommandTags().contains("TomahawkDiving")) {
                    float fallDist = player.fallDistance;
                    if (fallDist > 8.0f) triggerShockwave(player, fallDist);
                    player.removeCommandTag("TomahawkDiving");
                    player.fallDistance = 0;
                    return false;
                }
            }
            return true;
        });

        CustomRiptideEvent.EVENT.register((player, stack) -> {
            if (stack.getItem() instanceof TomahawkItem) {
                return Optional.of(Identifier.of(WorldbreakerProtocol.MOD_ID, "textures/entity/tomahawk_riptide.png"));
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
                        BlockPos markedPos = stack.get(ModComponents.MARKER_POS);

                        railItem.fireSkyBeam(player.getServerWorld(), player, markedPos, stack);

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
