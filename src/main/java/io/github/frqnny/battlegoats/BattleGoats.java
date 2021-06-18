package io.github.frqnny.battlegoats;

import io.github.frqnny.battlegoats.init.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class BattleGoats implements ModInitializer {
    public static final String MODID = "battle-goats";

    public static Identifier id(String namespace) {
        return new Identifier(MODID, namespace);
    }

    @Override
    public void onInitialize() {
        ModItems.init();
    }
}
