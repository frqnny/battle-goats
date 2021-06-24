package io.github.frqnny.battlegoats.entity.ai.task;

import com.google.common.collect.ImmutableMap;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
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

                return attacker != null && goat.canAttackWithOwner(attacker, livingEntity) && i != this.lastAttackedTime;
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
                goat.getModBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(attacker, 2.5F, 0));
                goat.getModBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(attacker, false));
                goat.getModBrain().remember(MemoryModuleType.RAM_TARGET, attacker.getPos());
                goat.getModBrain().forget(MemoryModuleType.RAM_COOLDOWN_TICKS);
                goat.getModBrain().doExclusively(Activity.RAM);
            }

        }


    }
}
