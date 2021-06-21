package io.github.frqnny.battlegoats.init;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import io.github.frqnny.battlegoats.client.gui.BattleGoatGUI;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;

public class ScreensBG {
    public static ScreenHandlerType<BattleGoatGUI> BATTLE_GOAT_GUI;

    public static void init() {
        BATTLE_GOAT_GUI = ScreenHandlerRegistry.registerExtended(BattleGoatGUI.ID, (syncId, inventory, buf) -> new BattleGoatGUI(syncId, inventory, ScreenHandlerContext.create(inventory.player.world, buf.readBlockPos()), buf.readInt()));
    }

    @SuppressWarnings("all")
    public static void clientInit() {
        ScreenRegistry.<BattleGoatGUI, CottonInventoryScreen<BattleGoatGUI>>register(BATTLE_GOAT_GUI, (gui, inventory, title) -> new CottonInventoryScreen<>(gui, inventory.player, title));
    }
}
