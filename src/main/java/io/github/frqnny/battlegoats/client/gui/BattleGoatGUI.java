package io.github.frqnny.battlegoats.client.gui;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.frqnny.battlegoats.BattleGoats;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import io.github.frqnny.battlegoats.init.ScreensBG;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.UUID;

public class BattleGoatGUI extends SyncedGuiDescription {
    public final ScreenHandlerContext ctx;
    public final BattleGoatEntity battleGoat;

    public static final Identifier ID = BattleGoats.id("battle_goats_gui");

    public BattleGoatGUI(int syncId, PlayerInventory playerInventory, ScreenHandlerContext ctx, int entityId) {
        super(ScreensBG.BATTLE_GOAT_GUI, syncId, playerInventory);
        this.ctx = ctx;
        Optional<Entity> optional = ctx.get((world, blockPos) -> world.getEntityById(entityId));
        if (optional.isPresent()) {
            battleGoat = (BattleGoatEntity) optional.get();
        } else throw new AssertionError("Failed to get BattleGoatEntity in GUI for: " + entityId +" and player: " + playerInventory.player.getDisplayName().asString());
        WGridPanel root = new WGridPanel(1);
        root.setSize(200, 200);
        root.setInsets(Insets.ROOT_PANEL);
        this.setRootPanel(root);


        WItemSlot slots = WItemSlot.outputOf(battleGoat.getInventory(), 0);
        WSprite saddleIcon = new WSprite(BattleGoats.id("textures/gui/saddle.png"));
        root.add(slots, 15, 120);
        root.add(saddleIcon, 16, 121, 16, 16);

        root.add(this.createPlayerInventoryPanel(), 15, 150);
        root.validate(this);
    }
}
