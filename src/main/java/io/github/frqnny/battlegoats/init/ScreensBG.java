package io.github.frqnny.battlegoats.init;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import io.github.frqnny.battlegoats.client.gui.BattleGoatGUI;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class ScreensBG {
    public static ScreenHandlerType<BattleGoatGUI> BATTLE_GOAT_GUI;

    public static void init() {
        BATTLE_GOAT_GUI = ScreenHandlerRegistry.registerExtended(BattleGoatGUI.ID, (syncId, inventory, buf) -> {
            BlockPos pos = buf.readBlockPos();
            int entityId = buf.readInt();
            BattleGoatEntity battleGoat;
            Optional<Entity> optional = Optional.ofNullable(inventory.player.world.getEntityById(entityId));
            if (optional.isPresent()) {
                battleGoat = (BattleGoatEntity) optional.get();
            } else
                throw new AssertionError("Failed to get BattleGoatEntity in GUI for: " + entityId + " and player: " + inventory.player.getDisplayName().asString());
            battleGoat.healthSkillLevel.fromBuf(buf);
            battleGoat.speedSkillLevel.fromBuf(buf);
            battleGoat.jumpSkillLevel.fromBuf(buf);
            battleGoat.attackDamageSkillLevel.fromBuf(buf);

            return new BattleGoatGUI(syncId, inventory, ScreenHandlerContext.create(inventory.player.world, pos), battleGoat);
        });
    }

    @SuppressWarnings("all")
    public static void clientInit() {
        ScreenRegistry.<BattleGoatGUI, CottonInventoryScreen<BattleGoatGUI>>register(BATTLE_GOAT_GUI, (gui, inventory, title) -> new CottonInventoryScreen<>(gui, inventory.player, title));
    }
}
