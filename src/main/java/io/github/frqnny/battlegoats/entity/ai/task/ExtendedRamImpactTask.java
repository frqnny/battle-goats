package io.github.frqnny.battlegoats.entity.ai.task;

import com.google.common.collect.ImmutableMap;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;


//TODO when attacking goat doesn't keep attacking targets
public class ExtendedRamImpactTask extends Task<BattleGoatEntity> {
    public static final int RUN_TIME = 200;
    public static final float SPEED_STRENGTH_MULTIPLIER = 1.65F;
    private final TargetPredicate targetPredicate;
    private final float speed;
    private final ToDoubleFunction<BattleGoatEntity> strengthMultiplierFactory;
    private final Function<BattleGoatEntity, SoundEvent> soundFactory;
    private Vec3d direction;

    public ExtendedRamImpactTask(TargetPredicate targetPredicate, float speed, ToDoubleFunction<BattleGoatEntity> strengthMultiplierFactory, Function<BattleGoatEntity, SoundEvent> soundFactory) {
        super(ImmutableMap.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.RAM_TARGET, MemoryModuleState.VALUE_PRESENT), RUN_TIME);
        this.targetPredicate = targetPredicate;
        this.speed = speed;
        this.strengthMultiplierFactory = strengthMultiplierFactory;
        this.soundFactory = soundFactory;
        this.direction = Vec3d.ZERO;
    }

    protected boolean shouldRun(ServerWorld serverWorld, BattleGoatEntity goat) {
        return goat.getBrain().hasMemoryModule(MemoryModuleType.RAM_TARGET) && !goat.hasPassengers();
    }

    protected boolean shouldKeepRunning(ServerWorld serverWorld, BattleGoatEntity goat, long l) {
        return goat.getBrain().hasMemoryModule(MemoryModuleType.RAM_TARGET) && !goat.hasPassengers();
    }

    protected void run(ServerWorld serverWorld, BattleGoatEntity goat, long l) {
        BlockPos blockPos = goat.getBlockPos();
        Brain<?> brain = goat.getBrain();
        Vec3d vec3d = brain.getOptionalMemory(MemoryModuleType.RAM_TARGET).get();
        this.direction = (new Vec3d((double) blockPos.getX() - vec3d.getX(), 0.0D, (double) blockPos.getZ() - vec3d.getZ())).normalize();
        brain.remember(MemoryModuleType.WALK_TARGET, (new WalkTarget(vec3d, this.speed, 0)));
    }

    protected void keepRunning(ServerWorld serverWorld, BattleGoatEntity goat, long l) {
        List<LivingEntity> list = serverWorld.getTargets(LivingEntity.class, this.targetPredicate, goat, goat.getBoundingBox().expand(0.2));
        Brain<?> brain = goat.getBrain();
        if (!list.isEmpty()) {
            LivingEntity target = list.get(0);
            if (target == goat.getOwner()) {
                if (list.size() >= 2) {
                    target = list.get(1);
                } else {
                    goat.getBrain().forget(MemoryModuleType.RAM_TARGET);
                    return;
                }
            }
            target.damage(DamageSource.mob(goat).setNeutral(), goat.getAttackDamage());
            float f = target.blockedByShield(DamageSource.mob(goat)) ? 0.5F : 1.0F;
            float g = MathHelper.clamp(goat.getMovementSpeed() * SPEED_STRENGTH_MULTIPLIER, 0.2F, 3.0F);
            target.takeKnockback((double) (f * g) * this.strengthMultiplierFactory.applyAsDouble(goat), this.direction.getX(), this.direction.getZ());
            this.moveTo(goat, target);
            serverWorld.sendEntityStatus(goat, (byte) 59);
            if (target.isDead()) {
                goat.getBrain().forget(MemoryModuleType.RAM_TARGET);
                this.moveTo(goat, goat.getOwner());
                goat.attackDamageSkillLevel.addXp(8);
            }
            goat.healthSkillLevel.addXp(2);
            serverWorld.playSoundFromEntity(null, goat, this.soundFactory.apply(goat), SoundCategory.HOSTILE, 1.0F, 1.0F);
        } else { // It missed
            if (goat.getOwner() instanceof LivingEntity owner) {
                if (owner.getAttacking() !=  null) {
                    this.moveTo(goat, owner.getAttacking());
                } else if (owner.getAttacker() != null) {
                    this.moveTo(goat, owner.getAttacker());
                } else {
                    brain.forget(MemoryModuleType.RAM_TARGET);

                }
            }
        }

    }


    public void moveTo(BattleGoatEntity goat, Entity entity) {
        goat.getModBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(entity, 2.5F, 0));
        goat.getModBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(entity, false));
    }

}
