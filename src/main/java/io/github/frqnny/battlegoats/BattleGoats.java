package io.github.frqnny.battlegoats;

import io.github.frqnny.battlegoats.init.ModItems;
import net.fabricmc.api.ModInitializer;

public class BattleGoats implements ModInitializer {
    @Override
    public void onInitialize() {
        ModItems.init();
    }
}
