package io.github.frqnny.battlegoats.init;

import io.github.frqnny.battlegoats.BattleGoats;
import io.github.frqnny.battlegoats.api.GadgetType;
import io.github.frqnny.battlegoats.item.GadgetItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.util.registry.Registry;

public class ItemsBG {
    public static final Item TECHY_GRAIN = new Item(new FabricItemSettings().group(BattleGoats.ITEM_GROUP));
    public static final Item REDSTONE_MILK = new MilkBucketItem(new FabricItemSettings().group(BattleGoats.ITEM_GROUP).recipeRemainder(Items.BUCKET).maxCount(1));
    public static final Item WINGS_GADGET = new GadgetItem(GadgetType.WINGS, new FabricItemSettings().maxCount(1).group(BattleGoats.ITEM_GROUP));
    public static final Item BOOSTER_GADGET = new GadgetItem(GadgetType.BOOSTERS, new FabricItemSettings().maxCount(1).group(BattleGoats.ITEM_GROUP));
    public static final Item STEEL_HORNS_GADGET = new GadgetItem(GadgetType.STEEL_HORNS, new FabricItemSettings().maxCount(1).group(BattleGoats.ITEM_GROUP));
    public static final Item GLASSES_GADGET = new GadgetItem(GadgetType.GLASSES, new FabricItemSettings().maxCount(1).group(BattleGoats.ITEM_GROUP));
    public static final Item MEGA_LEGS_GADGET = new GadgetItem(GadgetType.MEGA_LEGS, new FabricItemSettings().maxCount(1).group(BattleGoats.ITEM_GROUP));

    public static void init() {
        Registry<Item> r = Registry.ITEM;
        Registry.register(r, BattleGoats.id("techy_grain"), TECHY_GRAIN);
        Registry.register(r, BattleGoats.id("redstone_milk"), REDSTONE_MILK);
        Registry.register(r, BattleGoats.id("wings_gadget"), WINGS_GADGET);
        Registry.register(r, BattleGoats.id("booster_gadget"), BOOSTER_GADGET);
        Registry.register(r, BattleGoats.id("steel_horns_gadget"), STEEL_HORNS_GADGET);
        Registry.register(r, BattleGoats.id("glasses_gadget"), GLASSES_GADGET);
        Registry.register(r, BattleGoats.id("mega_legs_gadget"), MEGA_LEGS_GADGET);
    }
}
