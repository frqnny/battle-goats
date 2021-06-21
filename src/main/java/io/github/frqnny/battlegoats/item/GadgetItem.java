package io.github.frqnny.battlegoats.item;

import io.github.frqnny.battlegoats.api.GadgetType;
import net.minecraft.item.Item;

public class GadgetItem extends Item {
    public final GadgetType type;
    public GadgetItem(GadgetType type, Settings settings) {
        super(settings);
        this.type = type;
    }
}
