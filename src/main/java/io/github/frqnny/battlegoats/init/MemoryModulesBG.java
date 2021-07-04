package io.github.frqnny.battlegoats.init;

import io.github.frqnny.battlegoats.BattleGoats;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.util.registry.Registry;

import java.util.LinkedList;
import java.util.Optional;

public class MemoryModulesBG {
    public static final MemoryModuleType<LinkedList<WalkTarget>> RAM_TARGETS = new MemoryModuleType<>(Optional.empty());

    public static void init() {
        Registry<MemoryModuleType<?>> r = Registry.MEMORY_MODULE_TYPE;
        Registry.register(r, BattleGoats.id("ram_targets"), RAM_TARGETS);
    }
}
