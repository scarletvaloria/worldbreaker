package net.scarletvaloria.worldbreaker.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.scarletvaloria.worldbreaker.index.ModComponents;
import net.scarletvaloria.worldbreaker.index.ModDamageTypes;
import net.scarletvaloria.worldbreaker.index.ModParticles;
import net.scarletvaloria.worldbreaker.index.ModSounds;
import org.joml.Vector3f;

public class RailcannonItem extends Item {
    public RailcannonItem(Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking() && !world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;

            if (stack.contains(ModComponents.MARKER_POS)) {
                BlockPos markedPos = stack.get(ModComponents.MARKER_POS);
                this.fireSkyBeam(serverWorld, user, markedPos, stack);
                stack.remove(ModComponents.MARKER_POS);
                return TypedActionResult.success(stack);
            }

            Vec3d start = user.getEyePos();
            Vec3d end = start.add(user.getRotationVec(1.0f).multiply(64));

            EntityHitResult entityHit = ProjectileUtil.raycast(user, start, end, user.getBoundingBox().stretch(user.getRotationVec(1.0f).multiply(64)).expand(1.0), e -> !e.isSpectator() && e.canHit(), 4096.0);

            BlockPos targetPos = null;
            if (entityHit != null) {
                targetPos = entityHit.getEntity().getBlockPos();
            } else {
                BlockHitResult blockHit = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.ANY, user));
                if (blockHit.getType() == HitResult.Type.BLOCK) {
                    targetPos = blockHit.getBlockPos();
                }
            }

            if (targetPos != null) {
                stack.set(ModComponents.MARKER_POS, targetPos);
                user.sendMessage(Text.literal("§e§lTARGET LOCKED"), true);
                serverWorld.playSound(null, targetPos.getX(), targetPos.getY(), targetPos.getZ(), ModSounds.LOCK_ON, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            return TypedActionResult.success(stack);
        }
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingTicks) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            Vec3d start = player.getEyePos();
            Vec3d end = start.add(player.getRotationVec(1.0f).multiply(32));

            DamageSource laserSource = new DamageSource(world.getRegistryManager().getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE).getOrThrow(ModDamageTypes.RAILCANNON_KILL), player);

            EntityHitResult entityHit = ProjectileUtil.raycast(player, start, end, player.getBoundingBox().stretch(player.getRotationVec(1.0f).multiply(32)).expand(1.0), e -> !e.isSpectator() && e.canHit(), 1024.0);

            if (entityHit != null) {
                Entity target = entityHit.getEntity();
                target.damage(laserSource, 4.0F);
                target.setOnFireFor(3);
                this.spawnLaserParticles((ServerWorld)world, start, entityHit.getPos());
            } else {
                HitResult blockHit = player.raycast(32, 0, false);
                this.spawnLaserParticles((ServerWorld)world, start, blockHit.getPos());
            }

            if (remainingTicks % 20 == 0) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FIRE_LOOP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    public void fireSkyBeam(ServerWorld world, PlayerEntity user, BlockPos pos, ItemStack stack) {
        double radius = 3.0;

        for (int y = pos.getY(); y < world.getTopY(); y += 2) {
            for (int i = 0; i < 40; i++) {
                double angle = i * (2 * Math.PI / 40);
                double px = pos.getX() + 0.5 + Math.cos(angle) * radius;
                double pz = pos.getZ() + 0.5 + Math.sin(angle) * radius;
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, px, (double)y, pz, 1, 0, 0, 0, 0.05);
            }
        }

        Box damageArea = new Box(pos).expand(radius, 150, radius);
        DamageSource beamSource = new DamageSource(world.getRegistryManager().getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE).getOrThrow(ModDamageTypes.RAILCANNON_KILL), user);

        for (Entity target : world.getOtherEntities(user, damageArea)) {
            if (target instanceof LivingEntity living) {
                living.damage(beamSource, 30.0F);
                living.setOnFireFor(10);
                if (!living.isAlive()) {
                    world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, living.getX(), living.getY() + 1, living.getZ(), 5, 0.1, 0.1, 0.1, 0);
                }
            }
        }
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.SKYBEAM_HIT, SoundCategory.PLAYERS, 10.0f, 0.5f);
        user.getItemCooldownManager().set(this, 100);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && selected && entity instanceof PlayerEntity player) {
            BlockPos targetPos = stack.get(ModComponents.MARKER_POS);
            if (targetPos != null) {
                ServerWorld serverWorld = (ServerWorld) world;

                spawnGroundRing(serverWorld, targetPos);

                Vec3d start = getHandPos(player);
                Vec3d end = targetPos.toCenterPos();
                spawnGuidingLine(serverWorld, start, end);
            }
        }
    }

    private Vec3d getHandPos(PlayerEntity player) {
        Vec3d side = player.getRotationVec(1.0F).rotateY((float) Math.toRadians(-90));
        return player.getEyePos().add(player.getRotationVec(1.0F).multiply(0.5)).add(side.multiply(0.3)).subtract(0, 0.2, 0);
    }

    private void spawnGroundRing(ServerWorld sw, BlockPos p) {
        for (int i = 0; i < 20; i++) {
            double a = i * (Math.PI * 2 / 20);
            sw.spawnParticles(ModParticles.SHOCKWAVE_FLAME, p.getX() + 0.5 + Math.cos(a) * 3, p.getY() + 0.1, p.getZ() + 0.5 + Math.sin(a) * 3, 1, 0, 0, 0, 0);
        }
    }

    private void spawnGuidingLine(ServerWorld sw, Vec3d start, Vec3d end) {
        double dist = start.distanceTo(end);
        Vec3d dir = end.subtract(start).normalize();
        for (double i = 0.3; i < dist; i += 0.5) {
            Vec3d p = start.add(dir.multiply(i));
            sw.spawnParticles(new DustParticleEffect(new Vector3f(1.0f, 0.0f, 0.0f), 0.5f), p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    private void spawnLaserParticles(ServerWorld sw, Vec3d start, Vec3d end) {
        double dist = start.distanceTo(end);
        Vec3d dir = end.subtract(start).normalize();
        for (double i = 0.2; i < dist; i += 0.2) {
            Vec3d p = start.add(dir.multiply(i));
            sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 72000; }
}
