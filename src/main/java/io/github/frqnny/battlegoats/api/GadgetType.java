package io.github.frqnny.battlegoats.api;

import com.google.common.collect.Sets;

import java.util.Set;

public final record GadgetType(byte id) {
    public static final GadgetType WINGS = new GadgetType((byte) 0);
    public static final GadgetType BOOSTERS = new GadgetType((byte) 1);
    public static final GadgetType STEEL_HORNS = new GadgetType((byte) 2);
    public static final GadgetType GLASSES = new GadgetType((byte) 3);
    public static final GadgetType MEGA_LEGS = new GadgetType((byte) 4);

    public static final Set<GadgetType> values = Sets.newHashSet(WINGS, BOOSTERS, STEEL_HORNS, GLASSES, MEGA_LEGS);

}
