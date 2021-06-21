package io.github.frqnny.battlegoats.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import io.github.frqnny.battlegoats.BattleGoats;
import io.github.frqnny.battlegoats.client.gui.BattleGoatGUI;
import io.github.frqnny.battlegoats.entity.ai.BattleGoatBrain;
import io.github.frqnny.battlegoats.entity.inv.BattleGoatInventory;
import io.github.frqnny.battlegoats.init.EntitiesBG;
import io.github.frqnny.battlegoats.init.ItemsBG;
import io.github.frqnny.battlegoats.init.MemoryModulesBG;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SaddleItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
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

import javax.xml.crypto.Data;
import java.util.Optional;
import java.util.UUID;

public class BattleGoatEntity extends GoatEntity implements ExtendedScreenHandlerFactory, Tameable, Saddleable, ItemSteerable, JumpingMount {
    public static final Identifier ID = BattleGoats.id("battle_goat");
    protected static final ImmutableList<SensorType<? extends Sensor<? super GoatEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.GOAT_TEMPTATIONS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLING_DOWN, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleType.RAM_TARGET, MemoryModulesBG.OWNER);
    private final BattleGoatInventory inv;
    private static final TrackedData<Boolean> SADDLED = DataTracker.registerData(BattleGoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Boolean> SITTING = DataTracker.registerData(BattleGoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> BOOST_TIME = DataTracker.registerData(BattleGoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> GOAT_LEVEL = DataTracker.registerData(BattleGoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(BattleGoatEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    protected static final TrackedData<Boolean> ANGRY = DataTracker.registerData(BattleGoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    //TODO consider splitting up into Battle Level, Speed Level, Health?
    private final SaddledComponent saddledComponent;
    public boolean sitting = false;
    public boolean alreadyInteracted = false;
    protected float jumpStrength;
    private boolean jumping;
    protected boolean inAir;
    private int angryTicks;

    public BattleGoatEntity(EntityType<? extends GoatEntity> entityType, World world) {
        super(entityType, world);
        inv = new BattleGoatInventory(this);
        this.saddledComponent = new SaddledComponent(this.dataTracker, BOOST_TIME, SADDLED);
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
        } else {

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
                            System.out.println(this.sitting);
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
        return new BattleGoatGUI(syncId, inv, ScreenHandlerContext.create(this.world, this.getBlockPos()), this.getId());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.getBlockPos());
        buf.writeInt(this.getId());
    }

    @Nullable
    @Override
    public UUID getOwnerUuid() {
        return this.dataTracker.get(OWNER_UUID).orElse(null);
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
                LivingEntity livingEntity = (LivingEntity)this.getPrimaryPassenger();
                this.setYaw(livingEntity.getYaw());
                this.prevYaw = this.getYaw();
                this.setPitch(livingEntity.getPitch() * 0.5F);
                this.setRotation(this.getYaw(), this.getPitch());
                this.bodyYaw = this.getYaw();
                this.headYaw = this.bodyYaw;
                this.stepHeight = 1.0F;
                float f = livingEntity.sidewaysSpeed * 0.5F;
                float g = livingEntity.forwardSpeed;
                if (g <= 0.0F) {
                    g *= 0.25F;
                    //this.soundTicks = 0;
                }

                if (this.onGround && this.jumpStrength == 0.0F && this.isAngry() && !this.jumping) {
                    f = 0.0F;
                    g = 0.0F;
                }

                if (this.jumpStrength > 0.0F && !this.isInAir() && this.onGround) {
                    double d = this.jumpStrength * (double)this.jumpStrength * (double)this.getJumpVelocityMultiplier();
                    double h;
                    if (this.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                        h = d + (double)((float)(this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
                    } else {
                        h = d;
                    }

                    Vec3d vec3d = this.getVelocity();
                    this.setVelocity(vec3d.x, h, vec3d.z);
                    this.setInAir(true);
                    this.velocityDirty = true;
                    if (g > 0.0F) {
                        float i = MathHelper.sin(this.getYaw() * 0.017453292F);
                        float j = MathHelper.cos(this.getYaw() * 0.017453292F);
                        this.setVelocity(this.getVelocity().add((double)(-0.4F * i * this.jumpStrength), 0.0D, (double)(0.4F * j * this.jumpStrength)));
                    }

                    this.jumpStrength = 0.0F;
                }

                this.flyingSpeed = this.getMovementSpeed() * 0.1F;
                if (this.isLogicalSideForUpdatingMovement()) {
                    this.setMovementSpeed((float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
                    super.travel(new Vec3d(f, movementInput.y, g));
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

    @Override
    public boolean consumeOnAStickItem() {
        return this.saddledComponent.boost(this.getRandom());

    }

    @Override
    public void setMovementInput(Vec3d movementInput) {
        super.travel(movementInput);

    }

    @Override
    public float getSaddledSpeed() {
        //TODO implement speed level
        if (this.getPrimaryPassenger() instanceof LivingEntity entity) {
            return entity.forwardSpeed * 0.225F;
        }
        return 0;

    }

    public void onInvChange() {
        //TODO implement gadget update

        this.saddledComponent.setSaddled(inv.contains(stack -> stack.getItem() instanceof SaddleItem));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SADDLED, false);
        this.dataTracker.startTracking(BOOST_TIME, 0);
        this.dataTracker.startTracking(GOAT_LEVEL, 0);
        this.dataTracker.startTracking(SITTING, false);
        this.dataTracker.startTracking(OWNER_UUID, Optional.empty());
        this.dataTracker.startTracking(ANGRY, false);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        this.saddledComponent.writeNbt(nbt);
        inv.writeNbt(nbt);
        nbt.putBoolean("Sitting", this.dataTracker.get(SITTING));
        dataTracker.get(OWNER_UUID).ifPresent(uuid -> nbt.putUuid("Owner", uuid));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.saddledComponent.readNbt(nbt);
        inv.readNbt(nbt);
        this.dataTracker.set(SITTING, nbt.getBoolean("Sitting"));
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

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid));
    }


    @Override
    public void tickMovement() {
        if (!this.world.isClient()) {
            if (isSitting()) {
                this.dataTracker.set(SITTING, true);
            } else {
                this.dataTracker.set(SITTING, false);
                super.tickMovement();
            }
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
                this.jumpStrength = 0.4F + 0.4F * (float)strength / 90.0F;
            }

            this.jumpStrength += 0.05;
        }
    }

    @Override
    public boolean canJump() {
        return this.isSaddled();
    }

    @Override
    public void startJumping(int height) {

        this.jumping = true;
        //this.brain.doExclusively(Activity.LONG_JUMP);
    }

    public void stopJumping() {
    }

    public boolean isInAir() {
        return this.inAir;
    }

    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }

    public void setAngry(boolean angry) {
        this.dataTracker.set(ANGRY, angry);
    }

    private void updateAnger() {
        if (this.isLogicalSideForUpdatingMovement() || this.canMoveVoluntarily()) {
            this.angryTicks = 1;
            this.setAngry(true);
        }

    }

    public boolean isAngry() {
        return this.dataTracker.get(ANGRY);
    }

    public void tick() {
        super.tick();
        if ((this.isLogicalSideForUpdatingMovement() || this.canMoveVoluntarily()) && this.angryTicks > 0 && ++this.angryTicks > 20) {
            this.angryTicks = 0;
            this.setAngry(false);
        }




        if (!this.isAngry()) {
            this.jumping = false;

        }

    }
}
