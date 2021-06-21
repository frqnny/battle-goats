package io.github.frqnny.battlegoats.entity.ai.task;

import com.google.common.collect.ImmutableMap;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

//Updates task
public class SitTask extends Task<BattleGoatEntity> {
    public SitTask() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, BattleGoatEntity entity, long time) {
        return entity.isSitting();
    }

    @Override
    protected boolean shouldRun(ServerWorld world, BattleGoatEntity entity) {
        if (entity.isInsideWaterOrBubbleColumn()) {
            return false;
        } else if (!entity.isOnGround()) {
            return false;
        } else {
            LivingEntity livingEntity = (LivingEntity) entity.getOwner();
            if (livingEntity == null) {
                return true;
            } else {
                return (!(entity.squaredDistanceTo(livingEntity) < 144.0D) || livingEntity.getAttacker() == null) && entity.isSitting();
            }
        }
    }


    @Override
    protected void run(ServerWorld world, BattleGoatEntity entity, long time) {
        entity.getNavigation().stop();
        entity.getDataTracker().set(BattleGoatEntity.SITTING, true);
    }

    @Override
    protected void finishRunning(ServerWorld world, BattleGoatEntity entity, long time) {
        entity.getDataTracker().set(BattleGoatEntity.SITTING, false);
    }
}
