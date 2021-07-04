package io.github.frqnny.battlegoats.mixin;

import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public MixinPlayerEntityRenderer(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "setupTransforms", at = @At("HEAD"), cancellable = true)
    protected void goatTransforms(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrices, float f, float g, float tickDelta, CallbackInfo info) {
        if (abstractClientPlayerEntity.hasVehicle() && abstractClientPlayerEntity.getVehicle() instanceof BattleGoatEntity goat) {
            super.setupTransforms(abstractClientPlayerEntity, matrices, f, g, tickDelta);
            float n = (float) goat.getRoll() + tickDelta;
            float k = MathHelper.clamp(n * n / 100.0F, 0.0F, 1.0F);
            if (!goat.isUsingRiptide()) {
                matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(k * (-goat.getPitch())));
            }
            info.cancel();
        }
    }
}
