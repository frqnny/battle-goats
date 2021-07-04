package io.github.frqnny.battlegoats;

import io.github.frqnny.battlegoats.init.EntitiesBG;
import io.github.frqnny.battlegoats.init.NetworkingBG;
import io.github.frqnny.battlegoats.init.ScreensBG;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class BattleGoatsClient implements ClientModInitializer {
    public static final EntityModelLayer BATTLE_GOATS_LAYER = new EntityModelLayer(EntitiesBG.BATTLE_GOATS_LAYER_ID, "main");
    public static final EntityModelLayer BATTLE_GOATS_SADDLE_LAYER = new EntityModelLayer(EntitiesBG.BATTLE_GOATS_LAYER_ID, "saddle");

    @Override
    public void onInitializeClient() {
        EntitiesBG.clientInit();
        ScreensBG.clientInit();
        NetworkingBG.clientInit();
    }
}
