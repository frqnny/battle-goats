package io.github.frqnny.battlegoats.init;

import io.github.frqnny.battlegoats.BattleGoats;
import io.github.frqnny.battlegoats.BattleGoatsClient;
import io.github.frqnny.battlegoats.client.render.BattleGoatEntityRenderer;
import io.github.frqnny.battlegoats.client.render.model.BattleGoatEntityModel;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.model.Dilation;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EntitiesBG {
    public static final EntityType<BattleGoatEntity> BATTLE_GOAT = FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, BattleGoatEntity::new).dimensions(EntityDimensions.fixed(0.9F, 1.3F)).trackRangeChunks(10).build();

    public static final Identifier BATTLE_GOATS_LAYER_ID = BattleGoats.id("battle_goats");


    public static void init() {
        Registry.register(Registry.ENTITY_TYPE, BattleGoatEntity.ID, BATTLE_GOAT);
        FabricDefaultAttributeRegistry.register(BATTLE_GOAT, BattleGoatEntity.createBattleGoatAttributes());
    }


    public static void clientInit() {
        EntityRendererRegistry.INSTANCE.register(BATTLE_GOAT, BattleGoatEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(BattleGoatsClient.BATTLE_GOATS_LAYER, () -> BattleGoatEntityModel.getTexturedModelData(Dilation.NONE));
        EntityModelLayerRegistry.registerModelLayer(BattleGoatsClient.BATTLE_GOATS_SADDLE_LAYER, () -> BattleGoatEntityModel.getTexturedModelData(new Dilation(0.5F)));

    }
}
