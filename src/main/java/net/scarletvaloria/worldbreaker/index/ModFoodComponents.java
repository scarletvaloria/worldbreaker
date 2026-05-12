package net.scarletvaloria.worldbreaker.index;

import net.minecraft.component.type.FoodComponent;

public class ModFoodComponents {
    public static final FoodComponent PLASMA_CELL = new FoodComponent.Builder()
            .nutrition(18)
            .saturationModifier(3.0f)
            .alwaysEdible()
            .build();
}