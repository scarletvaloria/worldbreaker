package net.scarletvaloria.worldbreaker.index;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.model.TexturedModelData;

public class ModModelLayers {

    public static final EntityModelLayer WORLDBREAKER =
            new EntityModelLayer(
                    Identifier.of("worldbreaker", "worldbreaker"),
                    "main"
            );
}