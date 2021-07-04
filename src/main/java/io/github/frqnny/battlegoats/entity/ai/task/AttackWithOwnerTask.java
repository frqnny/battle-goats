package io.github.frqnny.battlegoats.entity.ai.task;

import com.google.common.collect.ImmutableMap;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import io.github.frqnny.battlegoats.util.RamUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;

public class AttackWithOwnerTask extends Task<BattleGoatEntity> {
    private int lastAttackTime;


    public AttackWithOwnerTask() {
        super(ImmutableMap.of());
    }


    @Override
    protected boolean shouldRun(ServerWorld world, BattleGoatEntity goat) {
        if (!goat.isSitting()) {
            if (goat.getOwner() instanceof LivingEntity owner) {
                int i = owner.getLastAttackTime();
                return i != this.lastAttackTime && goat.canAttackWithOwner(owner.getAttacking(), owner) && !goat.hasPassengers();
            }
        }


        return false;
    }


    @Override
    protected void run(ServerWorld world, BattleGoatEntity goat, long time) {
        if (goat.getOwner() instanceof LivingEntity owner) {
            LivingEntity attacking = owner.getAttacking();

            if (attacking != null) {
                this.lastAttackTime = owner.getLastAttackTime();
                RamUtils.rememberToAttackEntity(goat, attacking);
                goat.getModBrain().forget(MemoryModuleType.RAM_COOLDOWN_TICKS);
                goat.getModBrain().doExclusively(Activity.RAM);
            }

        }


    }
}
