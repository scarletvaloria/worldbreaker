package net.scarletvaloria.worldbreaker.item;

import com.terraformersmc.modmenu.util.mod.Mod;
import net.scarletvaloria.worldbreaker.index.ModParticles;
import net.acoyt.acornlib.api.item.CustomHitParticleItem;
import net.acoyt.acornlib.api.item.CustomHitSoundItem;
import net.acoyt.acornlib.api.item.KillEffectItem;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;
import net.scarletvaloria.worldbreaker.index.ModDamageTypes;
import net.scarletvaloria.worldbreaker.index.ModDataComponents;
import net.scarletvaloria.worldbreaker.index.ModSounds;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.scarletvaloria.worldbreaker.index.ModStatusEffects;

import java.util.List;
import java.util.UUID;

public class AMWDItem extends AxeItem implements CustomHitSoundItem, CustomHitParticleItem, KillEffectItem {
    private static final ToolMaterial toolMaterial = ToolMaterials.NETHERITE;

    public AMWDItem(Settings settings) {
        super(toolMaterial, settings);


    }

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 10.0f, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -3.2f, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
                        new EntityAttributeModifier(Identifier.of("worldbreaker", "range"), 0.5, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient) return;

        int useTime = this.getMaxUseTime(stack, user) - remainingUseTicks;

        if (useTime == 1) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    ModSounds.AMWD_CHARGE_START, SoundCategory.PLAYERS, 0.5f, 1.0f);
        }

        if (useTime % 10 == 0) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    ModSounds.AMWD_CHARGE_LOOP, SoundCategory.PLAYERS, 0.3f, 1.0f + (useTime * 0.01f));
        }
    }


    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient) return TypedActionResult.pass(stack);

        if (user.isSneaking()) {

            if (user.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                return TypedActionResult.fail(stack);
            }

            UUID id = user.getUuid();

            if (WorldbreakerProtocol.GRAVITY_DOMAIN_ACTIVE.contains(id)) {
                return TypedActionResult.fail(stack);
            }

            WorldbreakerProtocol.GRAVITY_DOMAIN_ACTIVE.add(id);
            WorldbreakerProtocol.GRAVITY_DOMAIN_TIMER.put(id, 200);

            user.getItemCooldownManager().set(stack.getItem(), 0);

            user.sendMessage(
                    Text.literal("§bGravitic Domain: ACTIVE"),
                    true
            );

            world.playSound(
                    null,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    ModSounds.AMWD_CHARGE_START,
                    SoundCategory.PLAYERS,
                    1.0f,
                    0.8f
            );

            return TypedActionResult.success(stack);
        }

        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (world.isClient) return;

        int chargeTime = this.getMaxUseTime(stack, user) - remainingUseTicks;
        if (chargeTime < 20) return;

        PlayerEntity player = (PlayerEntity) user;
        ServerWorld serverWorld = (ServerWorld) world;

        double radius = 10.0;

        Vec3d center = player.getPos();

        Box shockwaveBox = new Box(
                center.x - radius,
                center.y - radius,
                center.z - radius,
                center.x + radius,
                center.y + radius,
                center.z + radius
        );

        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                shockwaveBox,
                e -> e != player && e.squaredDistanceTo(player) <= (radius * radius)
        );

        for (LivingEntity target : targets) {
            DamageSource launchDamage = world.getDamageSources().create(ModDamageTypes.LAUNCH_KILL, player);
            target.damage(launchDamage, 16.0f);

            Vec3d diff = target.getPos().subtract(player.getPos());
            Vec3d horizontalDir = new Vec3d(diff.x, 0, diff.z).normalize();

            target.setVelocity(Vec3d.ZERO);

            target.addVelocity(horizontalDir.x * 4.0, 3.5, horizontalDir.z * 4.0);

            target.velocityModified = true;
            target.velocityDirty = true;

            world.playSound(null, target.getX(), target.getY(), target.getZ(),
                    ModSounds.WORLDBREAKER_SHOCKWAVE,
                    SoundCategory.PLAYERS,
                    1.5f,
                    1.2f
            );

            serverWorld.spawnParticles(
                    ModParticles.EXPANDING_RING,
                    target.getX(),
                    target.getY() + 0.05,
                    target.getZ(),
                    1,
                    0,
                    0,
                    0,
                    0
            );

            target.addStatusEffect(
                    new StatusEffectInstance(ModStatusEffects.CONCUSSED, 200, 0)
            );
        }

        player.getItemCooldownManager().set(stack.getItem(), 120);
    }





    @Override
    public void spawnHitParticles(PlayerEntity playerEntity, Entity entity) {

    }

    @Override
    public void playHitSound(PlayerEntity playerEntity, Entity entity) {

    }

    @Override
    public void killEntity(World world, ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity1) {

    }
}
