package io.github.frqnny.battlegoats.client.render;

import io.github.frqnny.battlegoats.BattleGoats;
import io.github.frqnny.battlegoats.client.render.model.BattleGoatEntityModel;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import io.github.frqnny.battlegoats.init.EntitiesBG;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class BattleGoatEntityRenderer extends MobEntityRenderer<BattleGoatEntity, BattleGoatEntityModel<BattleGoatEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/goat/goat.png");
    private static final Identifier SADDLE_TEXTURE = BattleGoats.id("textures/entity/goat_saddle.png");

    public BattleGoatEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BattleGoatEntityModel<>(ctx.getPart(EntitiesBG.BATTLE_GOATS_LAYER)), 0.7F);
        this.addFeature(new SaddleFeatureRenderer<>(this, new BattleGoatEntityModel<>(ctx.getPart(EntitiesBG.BATTLE_GOATS_SADDLE_LAYER)), SADDLE_TEXTURE));
        this.addFeature(new GadgetFeatureRenderer(this));
    }

    @Override
    public Identifier getTexture(BattleGoatEntity entity) {
        return TEXTURE;
    }


    @Override
    protected void setupTransforms(BattleGoatEntity goat, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
        float n;
        float k;
        if (goat.isFallFlying()) {
            super.setupTransforms(goat, matrices, animationProgress, bodyYaw, tickDelta);
            n = (float)goat.getRoll() + tickDelta;
            k = MathHelper.clamp(n * n / 100.0F, 0.0F, 1.0F);
            if (!goat.isUsingRiptide()) {
                matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(k * (- goat.getPitch())));
            }

            /* this code is broken in minecraft and me tyring to fix it is not fair
            Vec3d vec3d = goat.getRotationVec(tickDelta);
            Vec3d vec3d2 = goat.getVelocity();
            double d = vec3d2.horizontalLengthSquared();
            double e = vec3d.horizontalLengthSquared();
            if (d > 0.0D && e > 0.0D) {
                double l = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double m = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                float ok = (float)(Math.signum(m) * Math.acos(l));
                //weird stuff ok?
                float lol = ok * 20;
                int lolInt = (int) lol;
                float lmao = (float) lolInt;
                ok = lmao / 20F;
                System.out.println(ok);
                matrices.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(-ok));
            }

             */
        } else {
            super.setupTransforms(goat, matrices, animationProgress, bodyYaw, tickDelta);
        }
    }
}
