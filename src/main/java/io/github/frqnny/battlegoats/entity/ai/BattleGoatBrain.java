package io.github.frqnny.battlegoats.entity.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import io.github.frqnny.battlegoats.entity.ai.task.SitTask;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.passive.GoatBrain;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class BattleGoatBrain {
    //There's a bunch of fields here that I don't want to delete tbh, but I did so go check BattleGoatBrain
    private static final UniformIntProvider WALKING_SPEED = UniformIntProvider.create(5, 16);
    private static final UniformIntProvider LONG_JUMP_COOLDOWN_RANGE = UniformIntProvider.create(600, 1200);
    private static final UniformIntProvider RAM_COOLDOWN_RANGE = UniformIntProvider.create(600, 6000);
    private static final UniformIntProvider SCREAMING_RAM_COOLDOWN_RANGE = UniformIntProvider.create(100, 300);
    private static final TargetPredicate RAM_TARGET_PREDICATE = TargetPredicate.createAttackable().setPredicate((livingEntity) -> !livingEntity.getType().equals(EntityType.GOAT) && livingEntity.world.getWorldBorder().contains(livingEntity.getBoundingBox()));

    public static void resetLongJumpCooldown(BattleGoatEntity goat) {
        goat.getBrain().remember(MemoryModuleType.LONG_JUMP_COOLING_DOWN, LONG_JUMP_COOLDOWN_RANGE.get(goat.world.random));
        goat.getBrain().remember(MemoryModuleType.RAM_COOLDOWN_TICKS, RAM_COOLDOWN_RANGE.get(goat.world.random));
    }

    public static Brain<?> create(Brain<BattleGoatEntity> brain) {
        addCoreActivities(brain);
        addIdleActivities(brain);
        addLongJumpActivities(brain);
        addRamActivities(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(Brain<BattleGoatEntity> brain) {
        brain.setTaskList(Activity.CORE, 0,
                ImmutableList.of(
                        new StayAboveWaterTask(0.8F),
                        //new SitTask(),
                        new WalkTask(2.0F),
                        new LookAroundTask(45, 90),
                        new WanderAroundTask(),
                        new TemptationCooldownTask(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                        new TemptationCooldownTask(MemoryModuleType.LONG_JUMP_COOLING_DOWN),
                        new TemptationCooldownTask(MemoryModuleType.RAM_COOLDOWN_TICKS))
        );
    }

    private static void addIdleActivities(Brain<BattleGoatEntity> brain) {
        brain.setTaskList(Activity.IDLE,
                ImmutableList.of(
                        Pair.of(0, new TimeLimitedTask<>(new FollowMobTask(EntityType.PLAYER, 6.0F), UniformIntProvider.create(30, 60))),
                        Pair.of(0, new BreedTask(EntityType.GOAT, 1.0F)), Pair.of(1, new TemptTask((livingEntity) -> 1.25F)),
                        Pair.of(2, new WalkTowardClosestAdultTask<>(WALKING_SPEED, 1.25F)),
                        Pair.of(3, new RandomTask<>(
                                ImmutableList.of(
                                        Pair.of(new StrollTask(1.0F), 2),
                                        Pair.of(new GoTowardsLookTarget(1.0F, 3), 2),
                                        Pair.of(new WaitTask(30, 60), 1)
                                )
                        ))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.RAM_TARGET, MemoryModuleState.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleState.VALUE_ABSENT)
                )
        );
    }

    private static void addLongJumpActivities(Brain<BattleGoatEntity> brain) {
        brain.setTaskList(Activity.LONG_JUMP,
                ImmutableList.of(
                        Pair.of(0, new LeapingChargeTask(LONG_JUMP_COOLDOWN_RANGE, SoundEvents.ENTITY_GOAT_STEP)),
                        Pair.of(1, new LongJumpTask<>(LONG_JUMP_COOLDOWN_RANGE, 5, 5, 1.5F, (goatEntity) -> goatEntity.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_LONG_JUMP : SoundEvents.ENTITY_GOAT_LONG_JUMP))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryModuleState.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.BREED_TARGET, MemoryModuleState.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.LONG_JUMP_COOLING_DOWN, MemoryModuleState.VALUE_ABSENT)
                )
        );
    }

    private static void addRamActivities(Brain<BattleGoatEntity> brain) {
        brain.setTaskList(Activity.RAM,
                ImmutableList.of(
                        Pair.of(0, new RamImpactTask<>((goat) -> goat.isScreaming() ? SCREAMING_RAM_COOLDOWN_RANGE : RAM_COOLDOWN_RANGE, RAM_TARGET_PREDICATE, (goat) -> goat.isBaby() ? 1 : 2, 3.0F, (goatEntity) -> goatEntity.isBaby() ? 1.0D : 2.5D, (goatEntity) -> goatEntity.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_RAM_IMPACT : SoundEvents.ENTITY_GOAT_RAM_IMPACT)),
                        Pair.of(1, new PrepareRamTask<>((goatEntity) -> goatEntity.isScreaming() ? SCREAMING_RAM_COOLDOWN_RANGE.getMin() : RAM_COOLDOWN_RANGE.getMin(), 4, 7, 1.25F, RAM_TARGET_PREDICATE, 20, (goatEntity) -> goatEntity.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_PREPARE_RAM : SoundEvents.ENTITY_GOAT_PREPARE_RAM))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryModuleState.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.BREED_TARGET, MemoryModuleState.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT)
                )
        );
    }

    public static void updateActivities(BattleGoatEntity goat) {
        goat.getBrain().resetPossibleActivities(ImmutableList.of(Activity.RAM, Activity.LONG_JUMP, Activity.IDLE));
    }

    public static Ingredient getTemptItems() {
        return Ingredient.ofItems(Items.WHEAT);
    }
}

