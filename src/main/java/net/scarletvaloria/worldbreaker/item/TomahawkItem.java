package net.scarletvaloria.worldbreaker.item;

import net.acoyt.acornlib.api.event.CustomRiptideEvent;
import net.acoyt.acornlib.api.item.CustomHitSoundItem;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;
import net.scarletvaloria.worldbreaker.index.*;

import java.util.List;
import java.util.Optional;

public class TomahawkItem extends SwordItem implements CustomHitSoundItem, CustomRiptideEvent {
    public TomahawkItem(Settings settings) {
        super(ToolMaterials.NETHERITE, settings.attributeModifiers(TomahawkItem.createAttributeModifiers()));
    }

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 8.0f, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.4f, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
                        new EntityAttributeModifier(Identifier.ofVanilla("base_entity_interaction_range"), 0.25f, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (TomahawkState.canUse(stack) || user.isCreative()) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }

        return TypedActionResult.fail(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTime) {
        if (!(user instanceof PlayerEntity player)) return;

        int holdTime = getMaxUseTime(stack, user) - remainingUseTime;
        if (holdTime < 10) return;

        if (!world.isClient) {

            float f = -MathHelper.sin(player.getYaw() * 0.017453292F)
                    * MathHelper.cos(player.getPitch() * 0.017453292F);

            float g = -MathHelper.sin(player.getPitch() * 0.017453292F);

            float h = MathHelper.cos(player.getYaw() * 0.017453292F)
                    * MathHelper.cos(player.getPitch() * 0.017453292F);

            player.addVelocity(f * 3.0F, g * 3.0F, h * 3.0F);
            player.velocityModified = true;

            player.addCommandTag("TomahawkDiving");

            if (!player.isCreative()) {

                if (player instanceof ServerPlayerEntity serverPlayer) {
                    TomahawkItem.setCharges(serverPlayer, stack, TomahawkState.get(stack) - 1);
                }

                int remaining = TomahawkState.get(stack);

                player.getItemCooldownManager()
                        .set(this, remaining <= 0 ? 100 : 10);
            }
        }

        player.useRiptide(20, 8.0f, stack);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.TOMAHAWK_DASH, SoundCategory.MASTER, 2.0F, 1.0F);
    }

    public static void triggerShockwave(ServerPlayerEntity player, float intensity) {
        ServerWorld world = player.getServerWorld();

        var damageTypeRegistry = world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);
        var shockwaveEntry = damageTypeRegistry.getEntry(
                RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(WorldbreakerProtocol.MOD_ID, "shockwave_damage"))
        ).orElseThrow();

        DamageSource shockwaveSource = new DamageSource(shockwaveEntry, player);

        double radius = 8.0;
        float damage = 2.0f + (intensity * 0.2f);
        double knockbackStrength = 5;

        Box box = player.getBoundingBox().expand(radius, 3.0, radius);
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, box, e -> e != player);

        for (LivingEntity target : targets) {
            target.damage(shockwaveSource, damage);

            double dX = target.getX() - player.getX();
            double dZ = target.getZ() - player.getZ();
            double distance = Math.sqrt(dX * dX + dZ * dZ);

            if (distance > 0) {
                target.takeKnockback(knockbackStrength, -dX / distance, -dZ / distance);
                target.addVelocity(0, 0.9, 0);
                target.velocityModified = true;
            }
        }

        for (double currentRadius = 0.5; currentRadius <= radius; currentRadius += 1.5) {
            int density = (int) (currentRadius * 12);

            for (int i = 0; i < density; i++) {
                double angle = (2 * Math.PI / density) * i;
                double x = player.getX() + Math.cos(angle) * currentRadius;
                double z = player.getZ() + Math.sin(angle) * currentRadius;

                if (i % 2 == 0) {
                    world.spawnParticles(ModParticles.SHOCKWAVE_FLAME, x, player.getY() + 0.2, z, 5, 0.1, 0.1, 0.1, 0.02);
                } else {
                    world.spawnParticles(ParticleTypes.SMOKE, x, player.getY() + 0.1, z, 1, 0, 0, 0, 0);
                }
            }
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.WORLDBREAKER_SHOCKWAVE, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.max(0, Math.min(13,
                Math.round((TomahawkState.get(stack) * 13.0f) / 3.0f)
        ));
    }
    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0xFFD700;
    }

    @Override
    public void playHitSound(PlayerEntity playerEntity, Entity entity) {
    }


    @Override
    public Optional<Identifier> getRiptideTexture(PlayerEntity playerEntity, ItemStack itemStack) {
        return Optional.empty();
    }

    @Override
    public int getEnchantability() {
        return 0;
    }

    private static void syncHand(ServerPlayerEntity player) {
        int slot = player.getInventory().selectedSlot;

        player.getInventory().markDirty();

        player.networkHandler.sendPacket(
                new ScreenHandlerSlotUpdateS2CPacket(
                        -2,
                        0,
                        slot,
                        player.getMainHandStack().copy()
                )
        );
    }

    public static void setCharges(ServerPlayerEntity player, ItemStack stack, int value) {
        TomahawkState.set(stack, value);

        syncHand(player);
    }
}