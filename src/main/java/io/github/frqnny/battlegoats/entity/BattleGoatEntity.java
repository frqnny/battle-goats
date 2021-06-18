package io.github.frqnny.battlegoats.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import io.github.frqnny.battlegoats.BattleGoats;
import io.github.frqnny.battlegoats.entity.ai.BattleGoatBrain;
import io.github.frqnny.battlegoats.init.ModEntities;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BattleGoatEntity extends GoatEntity {
    public static final Identifier ID = BattleGoats.id("battle_goat");
    protected static final ImmutableList<SensorType<? extends Sensor<? super GoatEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.GOAT_TEMPTATIONS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLING_DOWN, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleType.RAM_TARGET);

    public BattleGoatEntity(EntityType<? extends GoatEntity> entityType, World world) {
        super(entityType, world);
    }

    //After some research, this only returns the brain variable (both getModBrain and getBrain) so I guess it's just to shut up compile errors? We don't have to override normal goat tasks to make them use this tldr
    public Brain<BattleGoatEntity> getModBrain() {
        return (Brain<BattleGoatEntity>) this.brain;
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return BattleGoatBrain.create(this.createModBrainProfile().deserialize(dynamic));

    }

    protected Brain.Profile<BattleGoatEntity> createModBrainProfile() {
        return Brain.createProfile(BattleGoatEntity.MEMORY_MODULES, BattleGoatEntity.SENSORS);
    }

    @Override
    protected void mobTick() {
        this.world.getProfiler().push("battleGoatBrain");
        this.getBrain().tick((ServerWorld) this.world, this);
        this.world.getProfiler().pop();
        this.world.getProfiler().push("goatActivityUpdate");
        BattleGoatBrain.updateActivities(this);
        this.world.getProfiler().pop();
        super.mobTick();
    }

    @Override
    public GoatEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        BattleGoatEntity goatEntity = ModEntities.BATTLE_GOAT.create(serverWorld);
        if (goatEntity != null) {
            BattleGoatBrain.resetLongJumpCooldown(goatEntity);
            boolean bl = passiveEntity instanceof GoatEntity && ((GoatEntity) passiveEntity).isScreaming();
            goatEntity.setScreaming(bl || serverWorld.getRandom().nextDouble() < 0.02D);
        }

        return goatEntity;
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        BattleGoatBrain.resetLongJumpCooldown(this);
        this.setScreaming(world.getRandom().nextDouble() < 0.02D);
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

}
