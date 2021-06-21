package io.github.frqnny.battlegoats.mixin;

import io.github.frqnny.battlegoats.client.gui.BattleGoatGUI;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import io.github.frqnny.battlegoats.init.EntitiesBG;
import io.github.frqnny.battlegoats.init.ItemsBG;
import io.github.frqnny.battlegoats.init.MemoryModulesBG;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

//TODO needs saving to tag
@Mixin(GoatEntity.class)
public abstract class MixinGoatEntity extends AnimalEntity {
    @Unique
    public int ticks = -1;
    @Unique
    public boolean isConverting = false;
    @Unique
    public UUID converter;

    protected MixinGoatEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    public void turnIntoBattleGoat(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> i) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem().equals(ItemsBG.TECHY_GRAIN)) {
            stack.decrement(1);

            ticks = 200;
            isConverting = true;
            converter = player.getUuid();
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, Math.min(this.world.getDifficulty().getId() - 1, 0)));
            this.world.sendEntityStatus(this, (byte)25);
        }
    }


    public void mobTick() {
        super.mobTick();

        if (!this.world.isClient()) {
            if (isConverting) {
                ticks--;
            }

            if (ticks == 0) {
                isConverting = false;
                this.remove(RemovalReason.DISCARDED);
                BattleGoatEntity goatEntity = EntitiesBG.BATTLE_GOAT.create(this.world);
                goatEntity.copyPositionAndRotation(this);
                goatEntity.getModBrain().remember(MemoryModulesBG.OWNER, uuid);
                goatEntity.initialize((ServerWorld) this.world, world.getLocalDifficulty(goatEntity.getBlockPos()), SpawnReason.CONVERSION, null, null);
                this.world.spawnEntity(goatEntity);
            }
        }
    }


    public void handleStatus(byte status) {
        if (status == 25) {
            if (!this.isSilent()) {
                this.world.playSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, this.getSoundCategory(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
            }
        } else {
            super.handleStatus(status);
        }
    }
}
