package net.scarletvaloria.worldbreaker.index;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class WorldbreakerFormManager {

    public static void transform(ServerPlayerEntity player) {
        ModComponents.INVENTORY.get(player).save(player.getInventory());
        player.getInventory().clear();

        player.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(1.25);
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(40.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(20.0);
        player.setHealth(40.0f);

        player.getInventory().setStack(0, new ItemStack(ModItems.TOMAHAWK));
        player.getInventory().setStack(1, new ItemStack(ModItems.WORLDBREAKER_RAILCANNON));
        player.getInventory().setStack(2, new ItemStack(ModItems.AMWD));
        player.getInventory().setStack(8, new ItemStack(ModItems.WORLDBREAKER_ASSEMBLY));
        player.getInventory().setStack(3, new ItemStack(ModItems.PLASMA_CELL, 64));
        player.getInventory().setStack(4, new ItemStack(ModItems.PLASMA_CELL, 64));

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0f, 1.0f);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, -1, 1, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, -1, 0, false, false));

        player.getAbilities().allowFlying = true;
        player.getAbilities().flying = true;
        player.getAbilities().setFlySpeed(0.15f);
        player.sendAbilitiesUpdate();

        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam("worldbreaker_team");

        if (team == null) {
            team = scoreboard.addTeam("worldbreaker_team");
        }

        team.setPrefix(Text.literal("Worldbreaker ").formatted(Formatting.WHITE));
        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);


        ModComponents.FORM_STATE.get(player).setActive(true);
        ModComponents.FORM_STATE.sync(player);
    }

    public static void revert(ServerPlayerEntity player) {
        ModComponents.INVENTORY.get(player).restore(player.getInventory());

        player.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(1.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(0.0);

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.5f);

        player.removeStatusEffect(StatusEffects.SPEED);
        player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        player.removeStatusEffect(StatusEffects.FIRE_RESISTANCE);

        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam("worldbreaker_team");
        if (team != null) {
            scoreboard.removeScoreHolderFromTeam(player.getNameForScoreboard(), team);
        }

        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().allowFlying = false;
            player.getAbilities().flying = false;
            player.getAbilities().setFlySpeed(0.05f);
            player.sendAbilitiesUpdate();
        }

        ModComponents.FORM_STATE.get(player).setActive(false);
    }
}
