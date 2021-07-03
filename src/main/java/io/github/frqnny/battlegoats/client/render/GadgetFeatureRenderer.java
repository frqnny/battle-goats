package io.github.frqnny.battlegoats.client.render;

import io.github.frqnny.battlegoats.api.GadgetType;
import io.github.frqnny.battlegoats.client.render.model.BattleGoatEntityModel;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import io.github.frqnny.battlegoats.init.ItemsBG;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;

public class GadgetFeatureRenderer extends FeatureRenderer<BattleGoatEntity, BattleGoatEntityModel<BattleGoatEntity>> {
    public static final ItemStack SUPER_LEGS = new ItemStack(ItemsBG.SUPER_LEG_INTERNAL);
    public static final ItemStack WINGS = new ItemStack(ItemsBG.WINGS_INTERNAL);


    public GadgetFeatureRenderer(FeatureRendererContext<BattleGoatEntity, BattleGoatEntityModel<BattleGoatEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BattleGoatEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        for (GadgetType type : entity.getEquippedGadgetTypes()) {
            if (GadgetType.STEEL_HORNS.equals(type)) {
                matrices.push();
                this.getContextModel().getHead().rotate(matrices);
                matrices.translate(-0.12,-0.65,-0.5);
                int lightAbove = WorldRenderer.getLightmapCoordinates(entity.world, entity.getBlockPos());
                MinecraftClient.getInstance().getItemRenderer().renderItem(SUPER_LEGS, ModelTransformation.Mode.GROUND, lightAbove, LivingEntityRenderer.getOverlay(entity, 0), matrices, vertexConsumers, 0);
                matrices.pop();
            } else if (GadgetType.WINGS.equals(type)) {
                matrices.push();
                this.getContextModel().getBody().rotate(matrices);
                matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
                matrices.translate(0,-0.65,0);
                int lightAbove = WorldRenderer.getLightmapCoordinates(entity.world, entity.getBlockPos());
                MinecraftClient.getInstance().getItemRenderer().renderItem(WINGS, ModelTransformation.Mode.GROUND, lightAbove, LivingEntityRenderer.getOverlay(entity, 0), matrices, vertexConsumers, 0);
                matrices.pop();
            }
        }
    }
}
