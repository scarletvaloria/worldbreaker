package net.scarletvaloria.worldbreaker.client.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class WorldbreakerModel extends EntityModel<Entity> {

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public WorldbreakerModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    @Override
    public void setAngles(Entity entity,
                          float limbSwing,
                          float limbSwingAmount,
                          float ageInTicks,
                          float netHeadYaw,
                          float headPitch) {

        head.yaw = netHeadYaw * 0.017453292F;
        head.pitch = headPitch * 0.017453292F;

        leftArm.pitch = MathHelper.cos(limbSwing * 0.6662F) * limbSwingAmount;
        rightArm.pitch = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * limbSwingAmount;

        leftLeg.pitch = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        rightLeg.pitch = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    }

    public void renderParts(net.minecraft.client.util.math.MatrixStack matrices,
                            net.minecraft.client.render.VertexConsumer vertices,
                            int light,
                            int overlay) {

        head.render(matrices, vertices, light, overlay);
        body.render(matrices, vertices, light, overlay);
        leftArm.render(matrices, vertices, light, overlay);
        rightArm.render(matrices, vertices, light, overlay);
        leftLeg.render(matrices, vertices, light, overlay);
        rightLeg.render(matrices, vertices, light, overlay);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {

    }
}