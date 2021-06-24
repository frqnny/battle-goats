package io.github.frqnny.battlegoats;

import io.github.frqnny.battlegoats.init.EntitiesBG;
import io.github.frqnny.battlegoats.init.ScreensBG;
import net.fabricmc.api.ClientModInitializer;

public class BattleGoatsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntitiesBG.clientInit();
        ScreensBG.clientInit();

    }
}
