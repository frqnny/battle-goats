package io.github.frqnny.battlegoats.init;

import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;

public class ModEntities {
    public static final EntityType<BattleGoatEntity> BATTLE_GOAT = FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, BattleGoatEntity::new).dimensions(EntityDimensions.fixed(0.9F, 1.3F)).trackRangeChunks(10).build();

    public static void init() {
        Registry.register(Registry.ENTITY_TYPE, BattleGoatEntity.ID, BATTLE_GOAT);
        FabricDefaultAttributeRegistry.register(BATTLE_GOAT, BattleGoatEntity.createGoatAttributes());
    }


    public static void clientInit() {

    }
}
