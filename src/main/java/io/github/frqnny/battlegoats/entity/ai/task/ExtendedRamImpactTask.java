package io.github.frqnny.battlegoats.entity.ai.task;

import com.google.common.collect.ImmutableMap;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.damage.DamageSource;
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
    private final ToIntFunction<BattleGoatEntity> damage;
    private final float speed;
    private final ToDoubleFunction<BattleGoatEntity> strengthMultiplierFactory;
    private final Function<BattleGoatEntity, SoundEvent> soundFactory;
    private Vec3d direction;

    public ExtendedRamImpactTask(TargetPredicate targetPredicate, ToIntFunction<BattleGoatEntity> damage, float speed, ToDoubleFunction<BattleGoatEntity> strengthMultiplierFactory, Function<BattleGoatEntity, SoundEvent> soundFactory) {
        super(ImmutableMap.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.RAM_TARGET, MemoryModuleState.VALUE_PRESENT), RUN_TIME);
        this.targetPredicate = targetPredicate;
        this.damage = damage;
        this.speed = speed;
        this.strengthMultiplierFactory = strengthMultiplierFactory;
        this.soundFactory = soundFactory;
        this.direction = Vec3d.ZERO;
    }

    protected boolean shouldRun(ServerWorld serverWorld, BattleGoatEntity goat) {
        return goat.getBrain().hasMemoryModule(MemoryModuleType.RAM_TARGET);
    }

    protected boolean shouldKeepRunning(ServerWorld serverWorld, BattleGoatEntity goat, long l) {
        return goat.getBrain().hasMemoryModule(MemoryModuleType.RAM_TARGET);
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
            LivingEntity livingEntity = list.get(0);
            if (livingEntity == goat.getOwner()) {
                goat.getBrain().forget(MemoryModuleType.RAM_TARGET);
                return;
            }
            livingEntity.damage(DamageSource.mob(goat).setNeutral(), (float) this.damage.applyAsInt(goat));
            float f = livingEntity.blockedByShield(DamageSource.mob(goat)) ? 0.5F : 1.0F;
            float g = MathHelper.clamp(goat.getMovementSpeed() * SPEED_STRENGTH_MULTIPLIER, 0.2F, 3.0F);
            livingEntity.takeKnockback((double) (f * g) * this.strengthMultiplierFactory.applyAsDouble(goat), this.direction.getX(), this.direction.getZ());
            serverWorld.sendEntityStatus(goat, (byte) 59);
            if (livingEntity.isDead()) {
                goat.getBrain().forget(MemoryModuleType.RAM_TARGET);
                goat.attackDamageSkillLevel.addXp(8);
            }
            goat.healthSkillLevel.addXp(2);
            serverWorld.playSoundFromEntity(null, goat, this.soundFactory.apply(goat), SoundCategory.HOSTILE, 1.0F, 1.0F);
        } else { // It missed
            if (goat.getOwner() instanceof LivingEntity owner) {
                if (owner.getAttacking() == null && owner.getAttacker() == null) {
                    brain.forget(MemoryModuleType.RAM_TARGET);
                }
            }
        }

    }

}
