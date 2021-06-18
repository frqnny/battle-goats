package io.github.frqnny.battlegoats;

import io.github.frqnny.battlegoats.init.ModEntities;
import net.fabricmc.api.ClientModInitializer;

public class BattleGoatsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModEntities.clientInit();
    }
}
