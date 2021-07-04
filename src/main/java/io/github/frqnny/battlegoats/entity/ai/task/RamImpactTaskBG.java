package io.github.frqnny.battlegoats.entity.ai.task;

import com.google.common.collect.ImmutableMap;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import io.github.frqnny.battlegoats.init.MemoryModulesBG;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;

public class RamImpactTaskBG extends Task<BattleGoatEntity> {
    public static final int RUN_TIME = 200;
    public static final float SPEED_STRENGTH_MULTIPLIER = 1.65F;
    private static final float speed = 3.0F;
    private Vec3d direction;


    public RamImpactTaskBG() {
        super(ImmutableMap.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT, MemoryModulesBG.RAM_TARGETS, MemoryModuleState.VALUE_PRESENT), RUN_TIME);
        this.direction = Vec3d.ZERO;
    }

    public static void moveTo(BattleGoatEntity goat, Entity entity) {
        goat.getModBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(entity, 2.5F, 0));
        goat.getModBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(entity, false));
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, BattleGoatEntity goat) {
        return goat.getBrain().hasMemoryModule(MemoryModulesBG.RAM_TARGETS) && !goat.hasPassengers();
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, BattleGoatEntity goat, long l) {
        return goat.getBrain().hasMemoryModule(MemoryModulesBG.RAM_TARGETS) && !goat.hasPassengers();
    }

    @Override
    protected void run(ServerWorld serverWorld, BattleGoatEntity goat, long l) {
        BlockPos blockPos = goat.getBlockPos();
        Brain<?> brain = goat.getBrain();
        Vec3d vec3d = brain.getOptionalMemory(MemoryModulesBG.RAM_TARGETS).get().getFirst().getLookTarget().getPos();
        this.direction = (new Vec3d((double) blockPos.getX() - vec3d.getX(), 0.0D, (double) blockPos.getZ() - vec3d.getZ())).normalize();
        brain.remember(MemoryModuleType.WALK_TARGET, (new WalkTarget(vec3d, speed, 0)));
    }

    @Override
    protected void keepRunning(ServerWorld world, BattleGoatEntity goat, long time) {
        super.keepRunning(world, goat, time);
        Brain<BattleGoatEntity> brain = goat.getModBrain();
        LinkedList<WalkTarget> walkTargets = brain.getOptionalMemory(MemoryModulesBG.RAM_TARGETS).orElseGet(LinkedList::new);
        if (!walkTargets.isEmpty()) {
            if (((EntityLookTarget) walkTargets.getFirst().getLookTarget()).getEntity() instanceof LivingEntity target) {
                moveTo(goat, target);
                if (goat.squaredDistanceTo(target) < 0.5) {
                    target.damage(DamageSource.mob(goat).setNeutral(), goat.getAttackDamage());
                    float f = target.blockedByShield(DamageSource.mob(goat)) ? 0.5F : 1.0F;
                    float g = MathHelper.clamp(goat.getMovementSpeed() * SPEED_STRENGTH_MULTIPLIER, 0.2F, 3.0F);
                    target.takeKnockback((double) (f * g) * 2.5F, this.direction.getX(), this.direction.getZ());

                    if (target.isDead()) {
                        walkTargets.removeFirst();
                        goat.attackDamageSkillLevel.addXp(8);
                        goat.healthSkillLevel.addXp(2);
                        if (!walkTargets.isEmpty()) {
                            moveTo(goat, ((EntityLookTarget) walkTargets.getFirst().getLookTarget()).getEntity());
                        }
                    }

                    world.playSoundFromEntity(null, goat, SoundEvents.ENTITY_GOAT_RAM_IMPACT, SoundCategory.HOSTILE, 1.0F, 1.0F);
                    world.sendEntityStatus(goat, (byte) 59);
                }
            }
            if (walkTargets.isEmpty()) {
                brain.forget(MemoryModulesBG.RAM_TARGETS);
                moveTo(goat, goat.getOwner());
            } else {
                brain.remember(MemoryModulesBG.RAM_TARGETS, walkTargets);
            }
        }

    }


}
