package net.scarletvaloria.worldbreaker.index;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WorldbreakerFormManager {

    public static void tick(ServerPlayerEntity player) {

        var form = ModComponents.FORM_STATE.get(player);
        if (form.getState() != WorldbreakerState.WORLDBREAKER) return;

        applyGraviticLockdown(player);
    }

    private static void applyGraviticLockdown(ServerPlayerEntity player) {

        double radius = 6.0;

        var world = player.getWorld();

        var entities = world.getEntitiesByClass(
                net.minecraft.entity.LivingEntity.class,
                player.getBoundingBox().expand(radius),
                e -> e != player
        );

        for (var target : entities) {

            if (target.isOnGround()) continue;

            var vel = target.getVelocity();

            double x = vel.x * 0.85;
            double z = vel.z * 0.85;
            double y = vel.y;

            if (y < -0.2) {
                y *= 0.55;
            }

            target.setVelocity(x, y, z);
            target.velocityModified = true;

            target.addVelocity(0, -0.03, 0);
        }
    }

    public static void onRespawn(ServerPlayerEntity player) {

        player.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(1.25);
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(40.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(40.0);

        player.setHealth(Math.min(player.getHealth(), 40.0f));

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, -1, 2, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAVING, -1, 255, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, -1, 1, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, -1, 0, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0, false, false, false));

        player.getAbilities().allowFlying = true;
        player.getAbilities().flying = true;
        player.getAbilities().setFlySpeed(0.08f);
        player.sendAbilitiesUpdate();
    }

    private static boolean isWorldbreakerItem(ItemStack stack) {
        return stack.isOf(ModItems.TOMAHAWK)
                || stack.isOf(ModItems.WORLDBREAKER_RAILCANNON)
                || stack.isOf(ModItems.AMWD)
                || stack.isOf(ModItems.PLASMA_CELL)
                || stack.isOf(ModItems.WORLDBREAKER_ASSEMBLY);
    }

    private static void clearWorldbreakerSlots(ServerPlayerEntity player) {
        PlayerInventory inv = player.getInventory();

        inv.setStack(0, ItemStack.EMPTY);
        inv.setStack(1, ItemStack.EMPTY);
        inv.setStack(2, ItemStack.EMPTY);
        inv.setStack(6, ItemStack.EMPTY);
        inv.setStack(7, ItemStack.EMPTY);
        inv.setStack(8, ItemStack.EMPTY);
    }

    public static void giveWorldbreakerLoadout(ServerPlayerEntity player) {

        clearWorldbreakerSlots(player);

        player.getInventory().setStack(0, markWorldbreaker(new ItemStack(ModItems.TOMAHAWK)));
        player.getInventory().setStack(1, markWorldbreaker(new ItemStack(ModItems.WORLDBREAKER_RAILCANNON)));
        player.getInventory().setStack(2, markWorldbreaker(new ItemStack(ModItems.AMWD)));

        player.getInventory().setStack(6, new ItemStack(ModItems.PLASMA_CELL, 64));
        player.getInventory().setStack(7, new ItemStack(ModItems.PLASMA_CELL, 64));

        player.getInventory().setStack(8, markWorldbreaker(new ItemStack(ModItems.WORLDBREAKER_ASSEMBLY)));
    }

    public static void transform(ServerPlayerEntity player) {

        var form = ModComponents.FORM_STATE.get(player);
        if (form.getState() != WorldbreakerState.NORMAL) return;

        var invComp = ModComponents.INVENTORY.get(player);

        consumeAssembly(player);

        invComp.clearStoredAssembly();

        invComp.save(player.getInventory());

        form.setState(WorldbreakerState.WORLDBREAKER);
        ModComponents.FORM_STATE.sync(player);

        player.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(1.25);
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(40.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(40.0);
        player.setHealth(40.0f);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, -1, 2, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAVING, -1, 255, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, -1, 1, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, -1, 0, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0, false, false, false));

        player.getAbilities().allowFlying = true;
        player.getAbilities().flying = true;
        player.getAbilities().setFlySpeed(0.08f);
        player.sendAbilitiesUpdate();

        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam("worldbreaker_team");

        if (team == null) {
            team = scoreboard.addTeam("worldbreaker_team");
        }

        team.setPrefix(Text.literal("Worldbreaker ")
                .styled(style -> style.withColor(Formatting.WHITE)));

        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);

        player.getInventory().clear();
        giveWorldbreakerLoadout(player);

        player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_BEACON_ACTIVATE,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );
    }

    public static void revert(ServerPlayerEntity player) {

        var form = ModComponents.FORM_STATE.get(player);
        if (form.getState() != WorldbreakerState.WORLDBREAKER) return;

        var invComp = ModComponents.INVENTORY.get(player);

        invComp.restore(player.getInventory());

        if (!player.getInventory().contains(stack ->
                stack.isOf(ModItems.WORLDBREAKER_ASSEMBLY))) {

            player.getInventory().insertStack(
                    new ItemStack(ModItems.WORLDBREAKER_ASSEMBLY)
            );
        }

        player.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(1.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(0.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);

        player.setHealth(Math.min(player.getHealth(), 20.0f));

        player.removeStatusEffect(StatusEffects.SPEED);
        player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        player.removeStatusEffect(StatusEffects.FIRE_RESISTANCE);
        player.removeStatusEffect(StatusEffects.RESISTANCE);
        player.removeStatusEffect(StatusEffects.WEAVING);

        player.getAbilities().allowFlying = false;
        player.getAbilities().flying = false;
        player.getAbilities().setFlySpeed(0.05f);
        player.sendAbilitiesUpdate();

        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam("worldbreaker_team");

        if (team != null) {
            scoreboard.removeScoreHolderFromTeam(player.getNameForScoreboard(), team);
        }

        form.setState(WorldbreakerState.NORMAL);
        ModComponents.FORM_STATE.sync(player);

        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_BEACON_DEACTIVATE,
                SoundCategory.BLOCKS,
                1.5f,
                1.0f
        );
    }

    private static void consumeAssembly(ServerPlayerEntity player) {
        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);

            if (stack.isOf(ModItems.WORLDBREAKER_ASSEMBLY)) {
                inv.setStack(i, ItemStack.EMPTY);
                inv.markDirty();
                return;
            }
        }
    }

    private static ItemStack markWorldbreaker(ItemStack stack) {
        stack.set(ModDataComponents.WORLDBREAKER_ITEM, true);
        return stack;
    }
}