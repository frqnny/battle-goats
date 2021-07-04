package io.github.frqnny.battlegoats.entity.ai.task;

import com.google.common.collect.ImmutableMap;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import io.github.frqnny.battlegoats.util.RamUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;

public class TrackOwnerAttackerTask extends Task<BattleGoatEntity> {
    private int lastAttackedTime;

    public TrackOwnerAttackerTask() {
        super(ImmutableMap.of());
    }


    @Override
    protected boolean shouldRun(ServerWorld world, BattleGoatEntity goat) {
        if (!goat.isSitting()) {
            Entity entity = goat.getOwner();

            if (entity instanceof LivingEntity livingEntity) {
                LivingEntity attacker = livingEntity.getAttacker();

                int i = livingEntity.getLastAttackedTime();

                return attacker != null && goat.canAttackWithOwner(attacker, livingEntity) && i != this.lastAttackedTime && !goat.hasPassengers();
            }
        }

        return false;
    }


    @Override
    protected void run(ServerWorld world, BattleGoatEntity goat, long time) {
        if (goat.getOwner() instanceof LivingEntity owner) {
            LivingEntity attacker = owner.getAttacker();

            if (attacker != null) {
                this.lastAttackedTime = owner.getLastAttackedTime();
                RamUtils.rememberToAttackEntity(goat, attacker);
                goat.getModBrain().forget(MemoryModuleType.RAM_COOLDOWN_TICKS);
                goat.getModBrain().doExclusively(Activity.RAM);
            }

        }


    }
}
