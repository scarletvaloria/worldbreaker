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
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.server.network.ServerPlayerEntity;

public class WorldbreakerFormManager {

    private static void addWorldbreakerPrefix(ServerPlayerEntity player) {
        Scoreboard scoreboard = player.getScoreboard();

        Team team = scoreboard.getTeam("worldbreaker_team");

        if (team == null) {
            team = scoreboard.addTeam("worldbreaker_team");

            team.setPrefix(
                    Text.literal("Worldbreaker ")
                            .formatted(Formatting.WHITE)
            );

            team.setColor(Formatting.WHITE);
        }

        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);
    }

    private static void removeWorldbreakerPrefix(ServerPlayerEntity player) {
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam("worldbreaker_team");

        if (team != null) {
            scoreboard.removeScoreHolderFromTeam(player.getNameForScoreboard(), team);
        }
    }

    public static void flashNearbyPlayers(ServerPlayerEntity player) {

        double radius = 30.0;
        double radiusSq = radius * radius;

        for (ServerPlayerEntity other : player.getServerWorld().getPlayers()) {

            if (other.squaredDistanceTo(player) > radiusSq) {
                continue;
            }

            ServerPlayNetworking.send(
                    other,
                    new WorldbreakerFlashPacket()
            );
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

    public static void transform(ServerPlayerEntity player) {

        var form = ModComponents.FORM_STATE.get(player);

        if (form.getState() != WorldbreakerState.NORMAL) {
            return;
        }

        ModComponents.INVENTORY
                .get(player)
                .save(player.getInventory());

        form.setState(WorldbreakerState.WORLDBREAKER);
        ModComponents.FORM_STATE.sync(player);

        addWorldbreakerPrefix(player);

        flashNearbyPlayers(player);

        player.getInventory().clear();

        giveLoadout(player);

        applyStats(player);

        playActivateSound(player);
    }

    public static void revert(ServerPlayerEntity player) {

        var form = ModComponents.FORM_STATE.get(player);

        if (form.getState() != WorldbreakerState.WORLDBREAKER) {
            return;
        }

        form.setState(WorldbreakerState.NORMAL);
        ModComponents.FORM_STATE.sync(player);

        removeWorldbreakerPrefix(player);

        flashNearbyPlayers(player);

        clearEffects(player);
        resetStats(player);

        player.getInventory().clear();

        ModComponents.INVENTORY
                .get(player)
                .restore(player.getInventory());

        player.playerScreenHandler.sendContentUpdates();

        playDeactivateSound(player);
    }

    private static void giveLoadout(ServerPlayerEntity player) {

        PlayerInventory inv = player.getInventory();

        inv.setStack(0, new ItemStack(ModItems.TOMAHAWK));
        inv.setStack(1, new ItemStack(ModItems.WORLDBREAKER_RAILCANNON));
        inv.setStack(2, new ItemStack(ModItems.AMWD));

        inv.setStack(6, new ItemStack(ModItems.PLASMA_CELL, 64));
        inv.setStack(7, new ItemStack(ModItems.PLASMA_CELL, 64));

        ItemStack assembly = new ItemStack(ModItems.WORLDBREAKER_ASSEMBLY);
        assembly.set(ModDataComponents.WORLDBREAKER_ITEM, true);

        inv.insertStack(assembly);
    }

    private static void applyStats(ServerPlayerEntity player) {

        player.getAttributeInstance(EntityAttributes.GENERIC_SCALE)
                .setBaseValue(1.25);

        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                .setBaseValue(40.0);

        player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR)
                .setBaseValue(40.0);

        player.setHealth(40.0f);

        player.addStatusEffect(
                new StatusEffectInstance(StatusEffects.SPEED, -1, 2, false, false, true)
        );

        player.addStatusEffect(
                new StatusEffectInstance(StatusEffects.RESISTANCE, -1, 0, false, false, false)
        );

        player.getAbilities().allowFlying = true;
        player.getAbilities().flying = false;
        player.getAbilities().setFlySpeed(0.1f);

        player.sendAbilitiesUpdate();
    }

    private static void clearEffects(ServerPlayerEntity player) {
        player.clearStatusEffects();
    }

    private static void resetStats(ServerPlayerEntity player) {

        player.getAttributeInstance(EntityAttributes.GENERIC_SCALE)
                .setBaseValue(1.0);

        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                .setBaseValue(20.0);

        player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR)
                .setBaseValue(0.0);

        player.setHealth(20.0f);

        player.getAbilities().allowFlying = false;
        player.getAbilities().flying = false;

        player.sendAbilitiesUpdate();
    }

    private static void playActivateSound(ServerPlayerEntity player) {

        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_BEACON_ACTIVATE,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );
    }

    private static void playDeactivateSound(ServerPlayerEntity player) {

        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_BEACON_DEACTIVATE,
                SoundCategory.PLAYERS,
                1.5f,
                1.0f
        );
    }
    public static void handleWorldbreakerDeath(ServerPlayerEntity player) {

        var form = ModComponents.FORM_STATE.get(player);
        if (form.getState() != WorldbreakerState.WORLDBREAKER) return;

        var invComp = ModComponents.INVENTORY.get(player);

        player.getInventory().clear();

        invComp.restore(player.getInventory());

        if (!player.getInventory().contains(stack ->
                stack.isOf(ModItems.WORLDBREAKER_ASSEMBLY))) {

            ItemStack assembly = new ItemStack(ModItems.WORLDBREAKER_ASSEMBLY);
            assembly.set(ModDataComponents.WORLDBREAKER_ITEM, true);
            player.getInventory().insertStack(assembly);
        }

        WorldbreakerProtocol.GRAVITY_DOMAIN_ACTIVE.remove(player.getUuid());
        WorldbreakerProtocol.GRAVITY_DOMAIN_TIMER.remove(player.getUuid());

        player.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(1.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(0.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.0);

        player.removeStatusEffect(StatusEffects.SPEED);
        player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        player.removeStatusEffect(StatusEffects.FIRE_RESISTANCE);
        player.removeStatusEffect(StatusEffects.RESISTANCE);
        player.removeStatusEffect(StatusEffects.WEAVING);

        player.getAbilities().allowFlying = false;
        player.getAbilities().flying = false;
        player.getAbilities().setFlySpeed(0.05f);
        player.sendAbilitiesUpdate();

        removeWorldbreakerPrefix(player);

        form.setState(WorldbreakerState.NORMAL);
        ModComponents.FORM_STATE.sync(player);
    }
}