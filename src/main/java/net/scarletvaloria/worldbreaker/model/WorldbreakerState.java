package net.scarletvaloria.worldbreaker.model;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.scarletvaloria.worldbreaker.index.ModComponents;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WorldbreakerState implements GeoAnimatable {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final AbstractClientPlayerEntity player;

    public WorldbreakerState(AbstractClientPlayerEntity player) {
        this.player = player;
    }

    public AbstractClientPlayerEntity getPlayer() {
        return player;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, event -> {

            if (!ModComponents.FORM_STATE.get(player).isActive())
                return PlayState.STOP;

            if (event.isMoving())
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));

            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object o) {
        return player.age + o.hashCode() * 0.0;
    }
}