package io.github.frqnny.battlegoats.init;

import io.github.frqnny.battlegoats.BattleGoats;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import net.minecraft.util.registry.Registry;

import java.util.Optional;
import java.util.UUID;

public class MemoryModulesBG {
    public static final MemoryModuleType<UUID> OWNER = new MemoryModuleType<>(Optional.of(DynamicSerializableUuid.CODEC));

    public static void init() {
        Registry<MemoryModuleType<?>> r = Registry.MEMORY_MODULE_TYPE;
        Registry.register(r, BattleGoats.id("owner"), OWNER);
    }
}
