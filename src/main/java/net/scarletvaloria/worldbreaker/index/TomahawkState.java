package net.scarletvaloria.worldbreaker.index;

import net.minecraft.item.ItemStack;
import net.scarletvaloria.worldbreaker.index.ModDataComponents;

public final class TomahawkState {

    private static final int MAX = 3;

    public static int get(ItemStack stack) {
        ensure(stack);
        return stack.get(ModDataComponents.DASH_CHARGES);
    }

    public static void set(ItemStack stack, int value) {
        stack.set(ModDataComponents.DASH_CHARGES,
                Math.max(0, Math.min(MAX, value)));
    }

    public static void ensure(ItemStack stack) {
        if (!stack.contains(ModDataComponents.DASH_CHARGES)) {
            stack.set(ModDataComponents.DASH_CHARGES, MAX);
        }
    }

    public static boolean canUse(ItemStack stack) {
        return get(stack) > 0;
    }

    public static void consume(ItemStack stack) {
        set(stack, get(stack) - 1);
    }

    public static void reset(ItemStack stack) {
        set(stack, MAX);
    }
}