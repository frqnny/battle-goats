package io.github.frqnny.battlegoats.client.gui;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.frqnny.battlegoats.BattleGoats;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import io.github.frqnny.battlegoats.init.ScreensBG;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.Identifier;

public class BattleGoatGUI extends SyncedGuiDescription {
    public static final Identifier ID = BattleGoats.id("battle_goats_gui");
    public final ScreenHandlerContext ctx;
    public final BattleGoatEntity battleGoat;

    public BattleGoatGUI(int syncId, PlayerInventory playerInventory, ScreenHandlerContext ctx, BattleGoatEntity battleGoat) {
        super(ScreensBG.BATTLE_GOAT_GUI, syncId, playerInventory);
        this.ctx = ctx;
        this.battleGoat = battleGoat;
        WGridPanel root = new WGridPanel(1);
        root.setSize(200, 200);
        root.setInsets(Insets.ROOT_PANEL);
        this.setRootPanel(root);

        WLabel healthLevel = new WLabel(String.valueOf(battleGoat.healthSkillLevel.level));
        root.add(healthLevel, 5, 15);
        WBar healthbar = new WBar(BattleGoats.id("textures/gui/xp_0.png"), BattleGoats.id("textures/gui/health_bar.png"), 0, 4, WBar.Direction.RIGHT);
        healthbar.setProperties(battleGoat.delegate);
        root.add(healthbar, 15, 15, 150, 10);

        WLabel speedLevel = new WLabel(String.valueOf(battleGoat.speedSkillLevel.level));
        root.add(speedLevel, 5, 30);
        WBar speedBar = new WBar(BattleGoats.id("textures/gui/xp_0.png"), BattleGoats.id("textures/gui/speed_bar.png"), 1, 5, WBar.Direction.RIGHT);
        speedBar.setProperties(battleGoat.delegate);
        root.add(speedBar, 15, 30, 150, 10);

        WLabel jumpLevel = new WLabel(String.valueOf(battleGoat.jumpSkillLevel.level));
        root.add(jumpLevel, 5, 45);
        WBar jumpBar = new WBar(BattleGoats.id("textures/gui/xp_0.png"), BattleGoats.id("textures/gui/jump_bar.png"), 2, 6, WBar.Direction.RIGHT);
        jumpBar.setProperties(battleGoat.delegate);
        root.add(jumpBar, 15, 45, 150, 10);

        WLabel attackDamageLevel = new WLabel(String.valueOf(battleGoat.attackDamageSkillLevel.level));
        root.add(attackDamageLevel, 5, 60);
        WBar attackDamageBar = new WBar(BattleGoats.id("textures/gui/xp_0.png"), BattleGoats.id("textures/gui/attack_damage_bar.png"), 3, 7, WBar.Direction.RIGHT);
        attackDamageBar.setProperties(battleGoat.delegate);
        root.add(attackDamageBar, 15, 60, 150, 10);


        WItemSlot saddle = WItemSlot.of(battleGoat.getInventory(), 0);
        WSprite saddleIcon = new WSprite(BattleGoats.id("textures/gui/saddle.png"));
        saddle.setModifiable(!battleGoat.isInSittingPose());
        root.add(saddle, 15, 120);
        root.add(saddleIcon, 16, 121, 16, 16);

        WItemSlot gadgets = WItemSlot.of(battleGoat.getInventory(), 1, 4, 2);
        root.add(gadgets, 100, 90);

        root.add(this.createPlayerInventoryPanel(), 15, 150);
        root.validate(this);
    }
}
