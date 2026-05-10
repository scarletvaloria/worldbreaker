package net.scarletvaloria.worldbreaker.index;

import net.minecraft.item.ItemStack;
import net.scarletvaloria.worldbreaker.index.ModDataComponents;

public class ChargeUtil {

    public static int getCharges(ItemStack stack) {
        if (!stack.contains(ModDataComponents.DASH_CHARGES)) {
            stack.set(ModDataComponents.DASH_CHARGES, 3);
        }
        return stack.get(ModDataComponents.DASH_CHARGES);
    }
}