package net.scarletvaloria.worldbreaker.index;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ConcussedEffect extends StatusEffect {
    public ConcussedEffect() {
        super(StatusEffectCategory.HARMFUL, 0x333333);
    }
}