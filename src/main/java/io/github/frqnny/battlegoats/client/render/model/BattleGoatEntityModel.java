package io.github.frqnny.battlegoats.client.render.model;

import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;

public class BattleGoatEntityModel<T extends BattleGoatEntity> extends QuadrupedEntityModel<T> {
    public BattleGoatEntityModel(ModelPart root) {
        super(root, true, 19.0F, 1.0F, 2.5F, 2.0F, 24);
    }

    public static TexturedModelData getTexturedModelData(Dilation dilation) {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData modelPartData2 = modelPartData.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(2, 61).cuboid("right ear", -6.0F, -11.0F, -10.0F, 3.0F, 2.0F, 1.0F, dilation).uv(2, 61).mirrored().cuboid("left ear", 2.0F, -11.0F, -10.0F, 3.0F, 2.0F, 1.0F, dilation).uv(23, 52).cuboid("goatee", -0.5F, -3.0F, -14.0F, 0.0F, 7.0F, 5.0F, dilation), ModelTransform.pivot(1.0F, 14.0F, 0.0F));
        modelPartData2.addChild(EntityModelPartNames.LEFT_HORN, ModelPartBuilder.create().uv(12, 55).cuboid(-0.01F, -16.0F, -10.0F, 2.0F, 7.0F, 2.0F, dilation), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        modelPartData2.addChild(EntityModelPartNames.RIGHT_HORN, ModelPartBuilder.create().uv(12, 55).cuboid(-2.99F, -16.0F, -10.0F, 2.0F, 7.0F, 2.0F, dilation), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        modelPartData2.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(34, 46).cuboid(-3.0F, -4.0F, -8.0F, 5.0F, 7.0F, 10.0F, dilation), ModelTransform.of(0.0F, -8.0F, -8.0F, 0.9599F, 0.0F, 0.0F));
        modelPartData.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(1, 1).cuboid(-4.0F, -17.0F, -7.0F, 9.0F, 11.0F, 16.0F, dilation).uv(0, 28).cuboid(-5.0F, -18.0F, -8.0F, 11.0F, 14.0F, 11.0F, dilation), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        modelPartData.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(36, 29).cuboid(0.0F, 4.0F, 0.0F, 3.0F, 6.0F, 3.0F, dilation), ModelTransform.pivot(1.0F, 14.0F, 4.0F));
        modelPartData.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(49, 29).cuboid(0.0F, 4.0F, 0.0F, 3.0F, 6.0F, 3.0F, dilation), ModelTransform.pivot(-3.0F, 14.0F, 4.0F));
        modelPartData.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(49, 2).cuboid(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, dilation), ModelTransform.pivot(1.0F, 14.0F, -6.0F));
        modelPartData.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(35, 2).cuboid(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, dilation), ModelTransform.pivot(-3.0F, 14.0F, -6.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(T goatEntity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.head.getChild(EntityModelPartNames.LEFT_HORN).visible = !goatEntity.isBaby();
        this.head.getChild(EntityModelPartNames.RIGHT_HORN).visible = !goatEntity.isBaby();
        super.setAngles(goatEntity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        float k = goatEntity.method_36283();
        if (k != 0.0F) {
            this.head.pitch = k;
        }

    }


    @Override
    public void animateModel(T entity, float limbAngle, float limbDistance, float tickDelta) {
        if (entity.isInSittingPose()) {
            this.head.setPivot(1.0F, 20.0F, 0.0F);
            this.body.setPivot(0.0F, 30.0F, 0.0F);
            this.leftHindLeg.setPivot(1.0F, 24.0F, 4.0F);
            this.leftHindLeg.roll = 4.71239F;
            this.rightHindLeg.setPivot(0.0F, 21.0F, 4.0F);
            this.rightHindLeg.roll = 1.5708F;
            this.leftFrontLeg.setPivot(4.0F, 24.0F, -6.0F);
            this.leftFrontLeg.roll = 4.71239F;
            this.rightFrontLeg.setPivot(-3.0F, 21.0F, -6.0F);
            this.rightFrontLeg.roll = 1.5708F;

        } else {
            this.head.setPivot(1.0F, 14.0F, 0.0F);
            this.body.setPivot(0.0F, 24.0F, 0.0F);
            this.leftHindLeg.setPivot(1.0F, 14.0F, 4.0F);
            this.leftHindLeg.roll = 0;
            this.rightHindLeg.setPivot(-3.0F, 14.0F, 4.0F);
            this.rightHindLeg.roll = 0;
            this.leftFrontLeg.setPivot(1.0F, 14.0F, -6.0F);
            this.leftFrontLeg.roll = 0;
            this.rightFrontLeg.setPivot(-3.0F, 14.0F, -6.0F);
            this.rightFrontLeg.roll = 0;

        }
    }


    public ModelPart getHead() {
        return this.head;
    }

    public ModelPart getBody() {
        return this.body;
    }
}

