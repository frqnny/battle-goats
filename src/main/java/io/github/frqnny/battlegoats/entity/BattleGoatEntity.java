package io.github.frqnny.battlegoats.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import io.github.frqnny.battlegoats.BattleGoats;
import io.github.frqnny.battlegoats.api.GadgetType;
import io.github.frqnny.battlegoats.api.skills.*;
import io.github.frqnny.battlegoats.client.gui.BattleGoatGUI;
import io.github.frqnny.battlegoats.entity.ai.BattleGoatBrain;
import io.github.frqnny.battlegoats.entity.inv.BattleGoatInventory;
import io.github.frqnny.battlegoats.init.EntitiesBG;
import io.github.frqnny.battlegoats.init.MemoryModulesBG;
import io.github.frqnny.battlegoats.item.GadgetItem;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SaddleItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class BattleGoatEntity extends GoatEntity implements ExtendedScreenHandlerFactory, Tameable, Saddleable, JumpingMount {
    public static final Identifier ID = BattleGoats.id("battle_goat");
    public static final TrackedData<Boolean> SITTING = DataTracker.registerData(BattleGoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected static final ImmutableList<SensorType<? extends Sensor<? super GoatEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.GOAT_TEMPTATIONS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLING_DOWN, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleType.RAM_TARGET, MemoryModulesBG.RAM_TARGETS);
    protected static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(BattleGoatEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    protected static final TrackedData<Boolean> JUMPING = DataTracker.registerData(BattleGoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> SADDLED = DataTracker.registerData(BattleGoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public final HealthSkillLevel healthSkillLevel = new HealthSkillLevel(this);
    public final SpeedSkillLevel speedSkillLevel = new SpeedSkillLevel(this);
    public final JumpSkillLevel jumpSkillLevel = new JumpSkillLevel();
    public final AttackDamageSkillLevel attackDamageSkillLevel = new AttackDamageSkillLevel(this);
    private final BattleGoatInventory inv;
    private final SaddledComponent saddledComponent;
    private final HashSet<GadgetType> gadgetSet = new HashSet<>(10);
    public PropertyDelegate delegate;
    public boolean sitting = false;
    public boolean alreadyInteracted = false;
    protected float jumpStrength;
    protected boolean inAir;
    private boolean jumping;
    private int jumpingTicks;
    private int speedTicks = -1;

    public BattleGoatEntity(EntityType<? extends GoatEntity> entityType, World world) {
        super(entityType, world);
        inv = new BattleGoatInventory(this);
        this.saddledComponent = new SaddledComponent(this.dataTracker, null, SADDLED);
        delegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                switch (index) {
                    case 0 -> {
                        return healthSkillLevel.xp;
                    }
                    case 1 -> {
                        return speedSkillLevel.xp;
                    }
                    case 2 -> {
                        return jumpSkillLevel.xp;
                    }
                    case 3 -> {
                        return attackDamageSkillLevel.xp;
                    }
                    case 4 -> {
                        return SkillLevel.xpNeededForLevel(healthSkillLevel.level);
                    }
                    case 5 -> {
                        return SkillLevel.xpNeededForLevel(speedSkillLevel.level);
                    }
                    case 6 -> {
                        return SkillLevel.xpNeededForLevel(jumpSkillLevel.level);
                    }
                    case 7 -> {
                        return SkillLevel.xpNeededForLevel(attackDamageSkillLevel.level);
                    }
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {

            }

            @Override
            public int size() {
                return 3;
            }
        };
    }

    public static DefaultAttributeContainer.Builder createBattleGoatAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.20000000298023224D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0D);
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
        this.getModBrain().tick((ServerWorld) this.world, this);
        this.world.getProfiler().pop();
        this.world.getProfiler().push("battleGoatActivityUpdate");
        BattleGoatBrain.updateActivities(this);
        this.world.getProfiler().pop();
        super.mobTick();
    }

    @Override
    public GoatEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        BattleGoatEntity goatEntity = EntitiesBG.BATTLE_GOAT.create(serverWorld);
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
        if (spawnReason != SpawnReason.CONVERSION) {
            this.setScreaming(world.getRandom().nextDouble() < 0.02D);
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.isBaby()) {
            if (this.isOwnedBy(player) && player.shouldCancelInteraction()) {
                player.openHandledScreen(this);
                return ActionResult.success(this.world.isClient);
            }

            if (this.hasPassengers()) {
                return super.interactMob(player, hand);
            }

            if (!this.world.isClient()) {
                ItemStack stack = player.getStackInHand(hand);
                if (stack.getItem().equals(Items.BEDROCK)) {
                    this.dataTracker.set(OWNER_UUID, Optional.ofNullable(player.getUuid()));
                    return ActionResult.PASS;
                }
                if (!this.isSaddled() && !this.hasPassengers() && !player.shouldCancelInteraction()) {
                    if (alreadyInteracted) {
                        alreadyInteracted = false;
                        return ActionResult.PASS;
                    } else {
                        ActionResult actionResult = super.interactMob(player, hand);
                        if ((!actionResult.isAccepted() || this.isBaby()) && this.isOwnedBy(player) && !(stack.getItem() instanceof SaddleItem)) {
                            this.setSitting(!sitting);
                            this.jumping = false;
                            this.navigation.stop();
                            this.setTarget(null);
                            this.alreadyInteracted = true;
                            return ActionResult.SUCCESS;
                        }

                        return actionResult;
                    }


                }
            }
            if (this.isSaddled() && !this.hasPassengers() && !player.shouldCancelInteraction()) {
                if (!this.world.isClient) {
                    player.startRiding(this);
                }

                return ActionResult.success(this.world.isClient);
            }


        }

        return super.interactMob(player, hand);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BattleGoatGUI(syncId, inv, ScreenHandlerContext.create(this.world, this.getBlockPos()), this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.getBlockPos());
        buf.writeInt(this.getId());
        healthSkillLevel.toBuf(buf);
        speedSkillLevel.toBuf(buf);
        jumpSkillLevel.toBuf(buf);
        attackDamageSkillLevel.toBuf(buf);
    }

    @Nullable
    @Override
    public UUID getOwnerUuid() {
        return this.dataTracker.get(OWNER_UUID).orElse(null);
    }

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    @Override
    public Entity getOwner() {
        try {
            UUID uUID = this.getOwnerUuid();
            return uUID == null ? null : this.world.getPlayerByUuid(uUID);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    public boolean isOwnedBy(PlayerEntity player) {
        UUID uuid = getOwnerUuid();

        if (uuid != null) {
            return player.getUuid().equals(uuid);
        }
        return false;
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isAlive()) {
            if (this.hasPassengers() && this.canBeControlledByRider() && this.isSaddled()) {
                LivingEntity livingEntity = (LivingEntity) this.getPrimaryPassenger();

                this.setYaw(livingEntity.getYaw());
                this.prevYaw = this.getYaw();
                this.setPitch(livingEntity.getPitch() * 0.5F);
                this.setRotation(this.getYaw(), this.getPitch());
                this.bodyYaw = this.getYaw();
                this.headYaw = this.bodyYaw;
                this.stepHeight = 1.0F;
                if (!this.world.isClient()) {
                    this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, this.hasGadgetEquipped(GadgetType.WINGS) && !this.isOnGround());
                }


                float sidewaysSpeed = livingEntity.sidewaysSpeed * 0.5F;
                float forwardSpeed = livingEntity.forwardSpeed;

                if (forwardSpeed != 0) {
                    if (speedTicks > 0) {
                        speedTicks--;
                    } else {
                        speedTicks = 20;
                        this.speedSkillLevel.addXp(1);
                    }
                }

                if (forwardSpeed <= 0.0F) {
                    forwardSpeed *= 0.25F;
                }

                if (this.onGround && this.jumpStrength == 0.0F && this.isAngry() && !this.jumping) {
                    sidewaysSpeed = 0.0F;
                    forwardSpeed = 0.0F;
                }

                boolean hasWingsEquipped = this.hasGadgetEquipped(GadgetType.WINGS);
                if (this.jumpStrength > 0.0F && (hasWingsEquipped || !this.isInAir() && this.onGround)) {
                    if (hasWingsEquipped) {
                        this.jumpStrength *= 0.75;
                    }
                    double d = this.jumpStrength * (double) this.jumpStrength * (double) this.getJumpVelocityMultiplier();
                    double h;
                    if (this.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                        h = d + (double) ((float) (this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
                    } else {
                        h = d;
                    }

                    Vec3d vec3d = this.getVelocity();
                    this.setVelocity(vec3d.x, h, vec3d.z);
                    this.setInAir(true);
                    this.velocityDirty = true;
                    if (forwardSpeed > 0.0F) {
                        float i = MathHelper.sin(this.getYaw() * 0.017453292F);
                        float j = MathHelper.cos(this.getYaw() * 0.017453292F);
                        this.setVelocity(this.getVelocity().add(-0.4F * i * this.jumpStrength, 0.0D, 0.4F * j * this.jumpStrength));
                    }
                    this.jumpStrength = 0.0F;
                }

                this.flyingSpeed = this.getMovementSpeed() * 0.1F;

                if (this.isLogicalSideForUpdatingMovement()) {

                    this.setMovementSpeed((float) this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
                    super.travel(new Vec3d(sidewaysSpeed, movementInput.y, forwardSpeed));
                } else if (livingEntity instanceof PlayerEntity) {
                    this.setVelocity(Vec3d.ZERO);
                }

                if (this.onGround) {
                    this.jumpStrength = 0.0F;
                    this.setInAir(false);
                }

                this.updateLimbs(this, false);
                this.tryCheckBlockCollision();
            } else {
                this.flyingSpeed = 0.02F;
                super.travel(movementInput);
            }
        }
    }

    @Override
    public boolean canBeSaddled() {
        return this.isAlive() && !this.isBaby() && !this.isInSittingPose();
    }

    @Override
    public void saddle(@Nullable SoundCategory sound) {
        this.inv.setStack(0, new ItemStack(Items.SADDLE));
        this.saddledComponent.setSaddled(true);
        if (sound != null) {
            this.world.playSoundFromEntity(null, this, SoundEvents.ENTITY_HORSE_SADDLE, sound, 0.5F, 1.0F);
        }
    }

    @Override
    public boolean isSaddled() {
        return this.saddledComponent.isSaddled();
    }

    public int getInvSize() {
        //TODO
        return 10;
    }

    public void onInvChange() {
        this.gadgetSet.clear();

        for (GadgetType gadgetType : GadgetType.values) {
            if (hasGadgetEquipped(gadgetType)) {
                gadgetSet.add(gadgetType);
            }
        }

        this.saddledComponent.setSaddled(inv.contains(stack -> stack.getItem() instanceof SaddleItem));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SADDLED, false);
        this.dataTracker.startTracking(SITTING, false);
        this.dataTracker.startTracking(OWNER_UUID, Optional.empty());
        this.dataTracker.startTracking(JUMPING, false);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        this.saddledComponent.writeNbt(nbt);
        inv.writeNbt(nbt);
        nbt.putBoolean("Sitting", sitting);
        dataTracker.get(OWNER_UUID).ifPresent(uuid -> nbt.putUuid("Owner", uuid));
        healthSkillLevel.writeNbt(nbt);
        speedSkillLevel.writeNbt(nbt);
        jumpSkillLevel.writeNbt(nbt);
        attackDamageSkillLevel.writeNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.saddledComponent.readNbt(nbt);
        inv.readNbt(nbt);
        sitting = nbt.getBoolean("Sitting");
        UUID uUID2;
        if (nbt.containsUuid("Owner")) {
            uUID2 = nbt.getUuid("Owner");
        } else {
            String string = nbt.getString("Owner");
            uUID2 = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
        }

        if (uUID2 != null) {
            try {
                this.setOwnerUuid(uUID2);
                //this.setTamed(true);
            } catch (Throwable var4) {
                //this.setTamed(false);
            }
        }
        healthSkillLevel.readNbt(nbt);
        speedSkillLevel.readNbt(nbt);
        jumpSkillLevel.readNbt(nbt);
        attackDamageSkillLevel.readNbt(nbt);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) { // avoids breeding by player
        return false;
    }

    @Override
    public boolean canBeControlledByRider() {
        //TODO check if its owner
        return true;
    }

    @Nullable
    public Entity getPrimaryPassenger() {
        return this.getFirstPassenger();
    }

    public Inventory getInventory() {
        return this.inv;
    }

    public boolean isSitting() {
        return this.sitting;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public boolean isInSittingPose() {
        return this.dataTracker.get(SITTING);
    }

    @Override
    public void tickMovement() {
        if (!this.world.isClient()) {
            if (!isSitting()) {
                this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, this.hasGadgetEquipped(GadgetType.WINGS) && !this.isOnGround());

                super.tickMovement();
            }
            this.dataTracker.set(SITTING, isSitting());
        } else {
            super.tickMovement();
        }

    }

    @Override
    public void setJumpStrength(int strength) {
        if (this.isSaddled()) {
            if (strength < 0) {
                strength = 0;
            } else {
                this.jumping = true;
                //this.updateAnger();
            }

            if (strength >= 90) {
                this.jumpStrength = 1.0F;
            } else {
                this.jumpStrength = 0.4F + 0.4F * (float) strength / 90.0F;
            }

            this.jumpStrength += jumpSkillLevel.getJumpStrength();
        }
    }

    @Override
    public boolean canJump() {
        return this.isSaddled();
    }

    @Override
    public void startJumping(int height) {
        this.jumping = true;
        int xp = (height * 5) / 100;
        this.jumpSkillLevel.addXp(xp > 2 ? xp : 0); // no grant 2 xp since it could be spam
        if (height > 40) {
            this.playSound(SoundEvents.ENTITY_GOAT_LONG_JUMP, 0.4F, 1.0F);
        }
    }

    public void stopJumping() {
    }

    public boolean isInAir() {
        return this.inAir;
    }

    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }


    public boolean isAngry() {
        return this.dataTracker.get(JUMPING);
    }

    public void setAngry(boolean angry) {
        this.dataTracker.set(JUMPING, angry);
    }

    public void tick() {
        super.tick();
        if (this.isOnGround()) {

        }
        if ((this.isLogicalSideForUpdatingMovement() || this.canMoveVoluntarily()) && this.jumpingTicks > 0 && ++this.jumpingTicks > 20) {
            this.jumpingTicks = 0;
            this.setAngry(false);
        }
        if (!this.isAngry()) {
            this.jumping = false;
        }
    }

    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        if (fallDistance > 1.0F) {
            this.playSound(SoundEvents.ENTITY_HORSE_LAND, 0.2F, 3.0F);
        }

        int i = this.computeFallDamage(fallDistance, damageMultiplier);
        if (i <= 0 || this.hasGadgetEquipped(GadgetType.WINGS)) {
            return false;
        } else {
            this.damage(damageSource, (float) i);
            if (this.hasPassengers()) {

                for (Entity entity : this.getPassengersDeep()) {
                    entity.damage(damageSource, (float) i);
                }
            }

            this.playBlockFallSound();
            return true;
        }
    }

    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        if (!(target instanceof CreeperEntity) && !(target instanceof GhastEntity)) {
            if (target instanceof WolfEntity wolfEntity) {
                return !wolfEntity.isTamed() || wolfEntity.getOwner() != owner;
            } else if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity) owner).shouldDamagePlayer((PlayerEntity) target)) {
                return false;
            } else if (target instanceof HorseBaseEntity && ((HorseBaseEntity) target).isTame()) {
                return false;
            } else {
                return !(target instanceof TameableEntity) || !((TameableEntity) target).isTamed();
            }
        } else {
            return false;
        }
    }

    public float getAttackDamage() {
        return (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) + (this.hasGadgetEquipped(GadgetType.STEEL_HORNS) ? 3.0F : 0.0F);
    }

    public Set<GadgetType> getEquippedGadgetTypes() {
        return gadgetSet;
    }

    public boolean hasGadgetEquipped(GadgetType type) {
        return this.inv.contains((stack) -> stack.getItem() instanceof GadgetItem gadgetItem && gadgetItem.type == type);
    }

    public void startFallFlying() {

        this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, true);
    }

    public void stopFallFlying() {
        this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, true);
        this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);
    }


    public boolean checkFallFlying() {
        if (!this.onGround && !this.isFallFlying() && !this.isTouchingWater() && !this.hasStatusEffect(StatusEffects.LEVITATION)) {
            if (this.hasGadgetEquipped(GadgetType.WINGS)) {
                this.startFallFlying();
                return true;
            }
        }

        return false;
    }

}
