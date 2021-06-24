package io.github.frqnny.battlegoats.client.render;

import io.github.frqnny.battlegoats.BattleGoats;
import io.github.frqnny.battlegoats.client.render.model.BattleGoatEntityModel;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import io.github.frqnny.battlegoats.init.EntitiesBG;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.util.Identifier;

public class BattleGoatEntityRenderer extends MobEntityRenderer<BattleGoatEntity, BattleGoatEntityModel<BattleGoatEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/goat/goat.png");
    private static final Identifier SADDLE_TEXTURE = BattleGoats.id("textures/entity/goat_saddle.png");

    public BattleGoatEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BattleGoatEntityModel<>(ctx.getPart(EntitiesBG.BATTLE_GOATS_LAYER)), 0.7F);
        this.addFeature(new SaddleFeatureRenderer<>(this, new BattleGoatEntityModel<>(ctx.getPart(EntitiesBG.BATTLE_GOATS_SADDLE_LAYER)), SADDLE_TEXTURE));
    }

    @Override
    public Identifier getTexture(BattleGoatEntity entity) {
        return TEXTURE;
    }
}
