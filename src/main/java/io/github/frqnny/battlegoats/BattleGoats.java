package io.github.frqnny.battlegoats;

import io.github.frqnny.battlegoats.init.EntitiesBG;
import io.github.frqnny.battlegoats.init.ItemsBG;
import io.github.frqnny.battlegoats.init.MemoryModulesBG;
import io.github.frqnny.battlegoats.init.ScreensBG;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class BattleGoats implements ModInitializer {
    public static final String MODID = "battle-goats";
    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            id("item_group"),
            () -> new ItemStack(ItemsBG.TECHY_GRAIN)
    );

    public static Identifier id(String namespace) {
        return new Identifier(MODID, namespace);
    }

    @Override
    public void onInitialize() {
        ItemsBG.init();
        MemoryModulesBG.init();
        EntitiesBG.init();
        ScreensBG.init();


    }
}
