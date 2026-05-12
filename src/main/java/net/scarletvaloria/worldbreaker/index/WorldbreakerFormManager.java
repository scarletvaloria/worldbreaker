package net.scarletvaloria.worldbreaker.index;

import net.minecraft.entity.EquipmentSlot;
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

public class WorldbreakerFormManager {

    public static void transform(ServerPlayerEntity player) {


        var form = ModComponents.FORM_STATE.get(player);
        if (form.getState() != WorldbreakerState.NORMAL) return;
        ItemStack oldChest = player.getEquippedStack(EquipmentSlot.CHEST).copy();

        if (!player.getInventory().contains(stack -> stack.isOf(ModItems.WORLDBREAKER_ASSEMBLY)))
            return;

        var inv = ModComponents.INVENTORY.get(player);

        inv.save(player.getInventory());
        player.getInventory().clear();

        player.getInventory().remove(
                stack -> stack.isOf(ModItems.WORLDBREAKER_ASSEMBLY),
                1,
                player.playerScreenHandler.getCraftingInput()
        );

        Scoreboard scoreboard = player.getScoreboard();

        Team team = scoreboard.getTeam("worldbreaker_team");
        if (team == null) {
            team = scoreboard.addTeam("worldbreaker_team");
        }

        team.setPrefix(Text.literal("Worldbreaker ").styled(style -> style.withColor(Formatting.WHITE)));

        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);

        player.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(1.25);
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(40.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(20.0);
        player.setHealth(40.0f);

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED,
                -1,
                2,
                false,
                false,
                true
        ));

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE,
                -1,
                0,
                false,
                false,
                true
        ));

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE,
                -1,
                0,
                false,
                false,
                true
        ));

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                -1,
                0,
                false,
                false,
                true
        ));

        player.getInventory().setStack(0, new ItemStack(ModItems.TOMAHAWK));
        player.getInventory().setStack(1, new ItemStack(ModItems.WORLDBREAKER_RAILCANNON));
        player.getInventory().setStack(2, new ItemStack(ModItems.AMWD));

        player.getInventory().setStack(6, new ItemStack(ModItems.PLASMA_CELL, 64));
        player.getInventory().setStack(7, new ItemStack(ModItems.PLASMA_CELL, 64));

        player.getInventory().setStack(8, new ItemStack(ModItems.WORLDBREAKER_ASSEMBLY));

        form.setState(WorldbreakerState.WORLDBREAKER);

        player.getAbilities().allowFlying = true;
        player.getAbilities().flying = true;
        player.getAbilities().setFlySpeed(0.08f);
        player.sendAbilitiesUpdate();

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

        player.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(1.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(0.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)
                .setBaseValue(1.0);

        player.setHealth(Math.min(player.getHealth(), 20.0f));

        player.removeStatusEffect(StatusEffects.SPEED);
        player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        player.removeStatusEffect(StatusEffects.FIRE_RESISTANCE);
        player.removeStatusEffect(StatusEffects.RESISTANCE);

        player.getAbilities().allowFlying = false;
        player.getAbilities().flying = false;
        player.getAbilities().setFlySpeed(0.05f);
        player.sendAbilitiesUpdate();

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

        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam("worldbreaker_team");
        if (team != null) {
            scoreboard.removeScoreHolderFromTeam(player.getNameForScoreboard(), team);
        }

        form.setState(WorldbreakerState.NORMAL);
        ModComponents.FORM_STATE.sync(player);
    }
}