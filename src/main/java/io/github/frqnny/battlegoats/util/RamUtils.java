package io.github.frqnny.battlegoats.util;

import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import io.github.frqnny.battlegoats.init.MemoryModulesBG;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.WalkTarget;

import java.util.LinkedList;

public class RamUtils {
    public static void rememberToAttackEntity(BattleGoatEntity goat, Entity target) {
        Brain<BattleGoatEntity> brain = goat.getModBrain();

        LinkedList<WalkTarget> walkTargets = brain.getOptionalMemory(MemoryModulesBG.RAM_TARGETS).orElseGet(LinkedList::new);
        walkTargets.add(new WalkTarget(target, 2.5F, 0));
        brain.remember(MemoryModulesBG.RAM_TARGETS, walkTargets);
    }
}
