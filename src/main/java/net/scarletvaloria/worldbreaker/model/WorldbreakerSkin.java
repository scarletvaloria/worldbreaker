package net.scarletvaloria.worldbreaker.model;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.scarletvaloria.worldbreaker.index.ModComponents;

public class WorldbreakerSkin {

    public static final Identifier WORLDBREAKER =
            Identifier.of("worldbreaker", "textures/entity/worldbreaker.png");

    public static Identifier get(AbstractClientPlayerEntity player) {
        if (ModComponents.FORM_STATE.get(player).isActive()) {
            return WORLDBREAKER;
        }

        return player.getSkinTextures().texture();
    }
}